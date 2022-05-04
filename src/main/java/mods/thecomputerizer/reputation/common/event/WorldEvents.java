package mods.thecomputerizer.reputation.common.event;

import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.PlayerFactionHandler;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.api.capability.IReputation;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import mods.thecomputerizer.reputation.common.ai.ReputationAIPackages;
import mods.thecomputerizer.reputation.common.ai.goals.FleeGoal;
import mods.thecomputerizer.reputation.common.ai.goals.ReputationAttackableTargetGoal;
import mods.thecomputerizer.reputation.common.ai.goals.ReputationPacifyHostileGoodStandingGoal;
import mods.thecomputerizer.reputation.common.ai.goals.ReputationPacifyHostileNeutralStandingGoal;
import mods.thecomputerizer.reputation.common.capability.PlayerFactionProvider;
import mods.thecomputerizer.reputation.common.command.AddPlayerToFactionCommand;
import mods.thecomputerizer.reputation.common.command.AddReputationCommand;
import mods.thecomputerizer.reputation.common.command.RemovePlayerFromFactionCommand;
import mods.thecomputerizer.reputation.common.command.SetReputationCommand;
import mods.thecomputerizer.reputation.common.network.PacketHandler;
import mods.thecomputerizer.reputation.common.network.SyncFactionsMessage;
import mods.thecomputerizer.reputation.common.registration.TagKeys;
import mods.thecomputerizer.reputation.util.HelperMethods;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

@Mod.EventBusSubscriber(modid = ModDefinitions.MODID)
public class WorldEvents {

    @SubscribeEvent
    public static void attachLevelCapabilities(AttachCapabilitiesEvent<Level> event) {
        Level level = event.getObject();
        if(level.dimension()==Level.OVERWORLD && !level.isClientSide()) {
            for (Faction faction : ReputationHandler.getFactionMap().values()) {
                PlayerFactionProvider playerFactionProvider = new PlayerFactionProvider(faction);
                PlayerFactionHandler.PLAYER_FACTIONS.put(faction, playerFactionProvider);
                event.addCapability(faction.getName(), playerFactionProvider);
            }
        }
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        SetReputationCommand.register(event.getDispatcher());
        AddReputationCommand.register(event.getDispatcher());
        AddPlayerToFactionCommand.register(event.getDispatcher());
        RemovePlayerFromFactionCommand.register(event.getDispatcher());
    }

    //attach reputation based goals to AI upon spawning
    @SubscribeEvent
    public static void onJoin(EntityJoinWorldEvent event) {
        if(event.getEntity() instanceof LivingEntity entity && !event.getWorld().isClientSide) {
            Brain<? extends LivingEntity> brain = entity.getBrain();
            brain.sensors.putIfAbsent(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_LIVING_ENTITIES.create());
            brain.memories.putIfAbsent(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, Optional.empty());
            if(entity instanceof Player player) {
                //sync faction data to players upon joining
                LazyOptional<IReputation> optional = player.getCapability(ReputationHandler.REPUTATION_CAPABILITY);
                if (optional.isPresent() && optional.resolve().isPresent()) {
                    IReputation reputation = optional.resolve().get();
                    Map<Faction, Integer> toSync = reputation.allReputations();
                    ServerPlayer serverPlayer = (ServerPlayer)player;
                    PacketHandler.sendTo(new SyncFactionsMessage(toSync.keySet()),serverPlayer);
                    for(Faction f : toSync.keySet()) {
                        reputation.setReputation(player,f,toSync.get(f));
                    }
                }
            }
            else {
                if (Objects.requireNonNull(ForgeRegistries.ENTITIES.tags()).getTag(TagKeys.FLEE).contains(entity.getType())) {
                    ReputationAIPackages.buildReputationSensor(brain);
                    ReputationAIPackages.buildReputationFleeAI(brain, HelperMethods.fleeFactor(entity));
                    if (entity instanceof Mob mob)
                        mob.goalSelector.addGoal(0, new FleeGoal(mob, HelperMethods.fleeFactor(entity)));
                }
                if (Objects.requireNonNull(ForgeRegistries.ENTITIES.tags()).getTag(TagKeys.HOSTILE).contains(entity.getType()) && entity instanceof Mob mob) {
                    ReputationAIPackages.buildReputationSensor(brain);
                    ReputationAIPackages.buildReputationHostileAI(mob, brain);
                    if (entity instanceof NeutralMob)
                        mob.targetSelector.addGoal(2, new ReputationAttackableTargetGoal<>(mob, Player.class, true, false));
                }
                if (Objects.requireNonNull(ForgeRegistries.ENTITIES.tags()).getTag(TagKeys.PASSIVE_NEUTRAL).contains(entity.getType())) {
                    ReputationAIPackages.buildReputationSensor(brain);
                    ReputationAIPackages.buildReputationPassiveNeutralAI(brain, 1);
                    if (entity instanceof Mob mob) {
                        Set<WrappedGoal> goalSet = mob.targetSelector.getAvailableGoals();
                        List<WrappedGoal> newGoals = goalSet.stream()
                                .filter((g) -> {
                                    if (g.getGoal() instanceof NearestAttackableTargetGoal targetGoal)
                                        return targetGoal.targetType != Player.class && targetGoal.targetType != ServerPlayer.class;
                                    return false;
                                }).toList();
                        mob.targetSelector.removeAllGoals();
                        for (WrappedGoal wrappedGoal : newGoals) {
                            mob.targetSelector.addGoal(wrappedGoal.getPriority(), wrappedGoal.getGoal());
                        }
                        mob.targetSelector.addGoal(2, new ReputationPacifyHostileNeutralStandingGoal<>(mob, Player.class, true, false));
                    }
                }
                if (Objects.requireNonNull(ForgeRegistries.ENTITIES.tags()).getTag(TagKeys.PASSIVE_GOOD).contains(entity.getType())) {
                    ReputationAIPackages.buildReputationSensor(brain);
                    ReputationAIPackages.buildReputationPassiveGoodAI(brain, 1);
                    if (entity instanceof Mob mob) {
                        Set<WrappedGoal> goalSet = mob.targetSelector.getAvailableGoals();
                        List<WrappedGoal> newGoals = goalSet.stream()
                                .filter((g) -> {
                                    if (g.getGoal() instanceof NearestAttackableTargetGoal targetGoal && !(g.getGoal() instanceof ReputationPacifyHostileNeutralStandingGoal))
                                        return targetGoal.targetType != Player.class && targetGoal.targetType != ServerPlayer.class;
                                    return false;
                                }).toList();
                        mob.targetSelector.removeAllGoals();
                        for (WrappedGoal wrappedGoal : newGoals) {
                            mob.targetSelector.addGoal(wrappedGoal.getPriority(), wrappedGoal.getGoal());
                        }
                        mob.targetSelector.addGoal(2, new ReputationPacifyHostileGoodStandingGoal<>(mob, Player.class, true, false));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player respawned = event.getPlayer();
        Brain<? extends LivingEntity> brain = respawned.getBrain();
        brain.sensors.putIfAbsent(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_LIVING_ENTITIES.create());
        brain.memories.putIfAbsent(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, Optional.empty());
        original.reviveCaps();
        for(Faction f : ReputationHandler.getFactionMap().values()) {
            respawned.getCapability(ReputationHandler.REPUTATION_CAPABILITY).orElseThrow(RuntimeException::new).setReputation(respawned,f,ReputationHandler.getReputation(original,f));
        }
        original.invalidateCaps();
    }

    @SubscribeEvent
    public static void onServerClose(ServerStoppedEvent e) {
        ReputationHandler.emptyMaps();
    }
}
