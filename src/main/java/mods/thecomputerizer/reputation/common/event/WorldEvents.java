package mods.thecomputerizer.reputation.common.event;

import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.PlayerFactionHandler;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.api.capability.IReputation;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import mods.thecomputerizer.reputation.common.ai.ChatTracker;
import mods.thecomputerizer.reputation.common.ai.ReputationAIPackages;
import mods.thecomputerizer.reputation.common.ai.ServerTrackers;
import mods.thecomputerizer.reputation.common.ai.goals.*;
import mods.thecomputerizer.reputation.common.capability.PlayerFactionProvider;
import mods.thecomputerizer.reputation.common.command.AddPlayerToFactionCommand;
import mods.thecomputerizer.reputation.common.command.AddReputationCommand;
import mods.thecomputerizer.reputation.common.command.RemovePlayerFromFactionCommand;
import mods.thecomputerizer.reputation.common.command.SetReputationCommand;
import mods.thecomputerizer.reputation.common.network.ChatIconMessage;
import mods.thecomputerizer.reputation.common.network.PacketHandler;
import mods.thecomputerizer.reputation.common.network.SyncFactionsMessage;
import mods.thecomputerizer.reputation.util.HelperMethods;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@SuppressWarnings("rawtypes")
@Mod.EventBusSubscriber(modid = ModDefinitions.MODID)
public class WorldEvents {
    private static int tickTimer = 0;
    public static final HashMap<LivingEntity, ChatTracker> trackers = new HashMap<>();
    private static final Random random = new Random();
    private static final List<ServerPlayer> players = new ArrayList<>();

    @SubscribeEvent
    public static void attachLevelCapabilities(AttachCapabilitiesEvent<Level> event) {
        //player factions have to be a separate capability attached to the overworld
        Level level = event.getObject();
        if(level.dimension()==Level.OVERWORLD && !level.isClientSide()) {
            for (Faction faction : ReputationHandler.getFactionMap().values()) {
                PlayerFactionProvider playerFactionProvider = new PlayerFactionProvider(faction);
                PlayerFactionHandler.PLAYER_FACTIONS.put(faction, playerFactionProvider);
                event.addCapability(faction.getID(), playerFactionProvider);
            }
        }
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        //register commands
        SetReputationCommand.register(event.getDispatcher());
        AddReputationCommand.register(event.getDispatcher());
        AddPlayerToFactionCommand.register(event.getDispatcher());
        RemovePlayerFromFactionCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinWorldEvent event) {
        if(event.getEntity() instanceof LivingEntity entity && !event.getWorld().isClientSide) {
            Brain<? extends LivingEntity> brain = entity.getBrain();
            //only need to add sensors to mobs that actually use brains, otherwise goals are necessary
            if(!brain.memories.isEmpty()) {
                brain.sensors.putIfAbsent(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_LIVING_ENTITIES.create());
                brain.memories.putIfAbsent(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, Optional.empty());
                ReputationAIPackages.buildReputationSensor(brain);
            }
            if(entity instanceof Player player) {
                //sync faction and chat icon data to players upon joining
                IReputation reputation = ReputationHandler.getCapability(player);
                if (Objects.nonNull(reputation)) {
                    HashMap<Faction, Integer> toSync = reputation.allReputations();
                    ServerPlayer serverPlayer = (ServerPlayer)player;
                    PacketHandler.sendTo(new SyncFactionsMessage(toSync,ReputationAIPackages.standings.getData()),serverPlayer);
                    for(Faction f : toSync.keySet()) reputation.setReputation(player,f,toSync.get(f));
                    ServerTrackers.syncChatIcons(serverPlayer);
                    players.add(serverPlayer);
                }
            }
            else {
                //reputation based AI stuff
                //makes hostile mobs not a part of the player's faction have a chance to flee from battle when injured
                if (ModDefinitions.INJURED_FLEEING_ENTITIES.contains(entity.getType())) {
                    if(!brain.memories.isEmpty()) ReputationAIPackages.buildReputationInjuredAI(brain,0.5f);
                    else if (entity instanceof PathfinderMob mob) {
                        FleeBattleGoal fleeBattleGoal = new FleeBattleGoal(mob, 0.5f);
                        mob.goalSelector.addGoal(0, fleeBattleGoal);
                        mob.targetSelector.addGoal(0,new FleeBattleTargetOverride(fleeBattleGoal,mob));
                    }
                }
                //makes passive mobs flee from players with specific reputation standings
                if (ModDefinitions.PASSIVE_FLEEING_ENTITIES.contains(entity.getType())) {
                    if(!brain.memories.isEmpty()) ReputationAIPackages.buildReputationFleeAI(brain, HelperMethods.fleeFactor(entity), ReputationAIPackages.standings.getPassiveFleeing(entity.getType()));
                    else if (entity instanceof Mob mob)
                        mob.goalSelector.addGoal(0, new FleeGoal(mob, HelperMethods.fleeFactor(entity), true));
                }
                //makes neutral mobs angry at players based on reputation standings
                if (ModDefinitions.HOSTILE_ENTITIES.contains(entity.getType()) && entity instanceof Mob mob) {
                    if(!brain.memories.isEmpty()) ReputationAIPackages.buildReputationHostileAI(mob, brain,ReputationAIPackages.standings.getHostile(entity.getType()));
                    else if (entity instanceof NeutralMob)
                        mob.targetSelector.addGoal(2, new ReputationAttackableTargetGoal<>(mob, Player.class, true, false));
                }
                //conditionally removes players from the target selection of hostile mobs based on reputation standings
                if (ModDefinitions.PASSIVE_ENTITIES.contains(entity.getType())) {
                    if(!brain.memories.isEmpty()) ReputationAIPackages.buildReputationPassiveAI(brain, 1,ReputationAIPackages.standings.getPassive(entity.getType()));
                    else if (entity instanceof Mob mob) {
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
                        mob.targetSelector.addGoal(2, new ReputationPacifyHostileCustomStandingGoal<>(mob, Player.class, true, false,ReputationAIPackages.standings.getPassive(entity.getType())));
                    }
                }
            }
            //finalize trackers for chat icons
            if(!ReputationHandler.getEntityFactions(entity).isEmpty()) trackers.put(entity,new ChatTracker(entity));
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.Clone event) {
        //reattach the reputation capability to respawning players
        Player original = event.getOriginal();
        Player respawned = event.getPlayer();
        if(original instanceof ServerPlayer serverOriginal && respawned instanceof  ServerPlayer serverRespawned) {
            players.remove(serverOriginal);
            players.add(serverRespawned);
        }
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

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if(e.phase== TickEvent.Phase.END) {
            if(ServerTrackers.iconsLoaded) {
                tickTimer++;
                //remove trackers for entities that do not have any chat icons loaded
                trackers.entrySet().removeIf(tracker -> !ServerTrackers.serverIconMap.containsKey(tracker.getKey().getType()) || ServerTrackers.serverIconMap.get(tracker.getKey().getType()).isEmpty());
                for (ChatTracker tracker : trackers.values()) tracker.queryChatTimer();
                //only check and sync chat icon trackers once a second for performance purposes
                if (tickTimer >= 20) {
                    long seed = random.nextLong(Long.MAX_VALUE);
                    ArrayList<ChatTracker> toUpdate = new ArrayList<>();
                    for (LivingEntity entity : trackers.keySet()) {
                        ChatTracker tracker = trackers.get(entity);
                        if (seed >= tracker.getSeed() && !tracker.getRecent()) {
                            //check and set the idle event
                            if (!tracker.getRandom() && ServerTrackers.hasIconsForEvent(tracker.getEntityType(), "idle")) {
                                tracker.setRandom(true);
                                tracker.setChanged(true);
                                tracker.setRecent(true);
                            }
                            //check and set the idle_faction event
                            if (!tracker.getInRange() && ServerTrackers.hasIconsForEvent(tracker.getEntityType(), "idle_faction")) {
                                Level level = entity.getLevel();
                                if (level instanceof ServerLevel serverLevel) {
                                    for (Faction f : ReputationHandler.getEntityFactions(entity)) {
                                        if (!HelperMethods.getSeenEntitiesOfFaction(serverLevel, entity, 16, entity.getBrain(), f).isEmpty()) {
                                            tracker.setInRange(true);
                                            tracker.setChanged(true);
                                            tracker.setRecent(true);
                                        }
                                    }
                                }
                            }
                        }
                        if (tracker.getChanged()) toUpdate.add(tracker);
                    }
                    //only sync chat icon trackers with changes to the client for performance purposes
                    if (!toUpdate.isEmpty()) {
                        for (ServerPlayer player : players) PacketHandler.sendTo(new ChatIconMessage(toUpdate), player);
                        for (ChatTracker tracker : toUpdate) {
                            tracker.setRandom(false);
                            tracker.setInRange(false);
                            tracker.setEngage(false);
                            tracker.setFlee(false);
                            tracker.setChanged(false);
                        }
                    }
                    tickTimer = 0;
                }
            }
        }
    }
}
