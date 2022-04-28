package mods.thecomputerizer.reputation.common.event;

import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.api.ContainerHandler;
import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.PlayerFactionHandler;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import mods.thecomputerizer.reputation.common.ai.ReputationAIPackages;
import mods.thecomputerizer.reputation.common.ai.goals.FleeGoal;
import mods.thecomputerizer.reputation.common.ai.goals.ReputationAttackableTargetGoal;
import mods.thecomputerizer.reputation.common.ai.goals.ReputationPacifyHostileGoodStandingGoal;
import mods.thecomputerizer.reputation.common.ai.goals.ReputationPacifyHostileNeutralStandingGoal;
import mods.thecomputerizer.reputation.common.capability.PlacedContainerProvider;
import mods.thecomputerizer.reputation.common.capability.PlayerFactionProvider;
import mods.thecomputerizer.reputation.common.capability.ReputationProvider;
import mods.thecomputerizer.reputation.common.command.AddPlayerToFactionCommand;
import mods.thecomputerizer.reputation.common.command.AddReputationCommand;
import mods.thecomputerizer.reputation.common.command.RemovePlayerFromFactionCommand;
import mods.thecomputerizer.reputation.common.command.SetReputationCommand;
import mods.thecomputerizer.reputation.common.network.PacketHandler;
import mods.thecomputerizer.reputation.common.network.SyncFactionsMessage;
import mods.thecomputerizer.reputation.common.registration.TagKeys;
import mods.thecomputerizer.reputation.config.ClientConfigHandler;
import mods.thecomputerizer.reputation.util.HelperMethods;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = ModDefinitions.MODID)
public class ReputationEvents {

	@SubscribeEvent
	public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (entity instanceof Player & !(entity instanceof FakePlayer)) {
			event.addCapability(ModDefinitions.getResource("reputation"), new ReputationProvider());
		} else if (entity instanceof Container)
			event.addCapability(ModDefinitions.getResource("container_placed"), new PlacedContainerProvider());
	}

	@SubscribeEvent
	public static void attachLevelCapabilities(AttachCapabilitiesEvent<Level> event) {
		Level level = event.getObject();
		if(level.dimension()==Level.OVERWORLD && !level.isClientSide()) {
			for (Faction faction : ReputationHandler.getFactions()) {
				PlayerFactionProvider playerFactionProvider = new PlayerFactionProvider(faction);
				PlayerFactionHandler.PLAYER_FACTIONS.put(faction, playerFactionProvider);
				event.addCapability(faction.getName(), playerFactionProvider);
			}
		}
	}

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event){
		SetReputationCommand.register(event.getDispatcher());
		AddReputationCommand.register(event.getDispatcher());
		AddPlayerToFactionCommand.register(event.getDispatcher());
		RemovePlayerFromFactionCommand.register(event.getDispatcher());
	}

	@SubscribeEvent
	public static void onPlayerJoin(PlayerLoggedInEvent event) {
		Player player = event.getPlayer();
		if (!player.level.isClientSide) {
			Brain<? extends LivingEntity> brain = player.getBrain();
			brain.sensors.putIfAbsent(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_LIVING_ENTITIES.create());
			brain.memories.putIfAbsent(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, Optional.empty());
			PacketHandler.NETWORK_INSTANCE.sendTo(new SyncFactionsMessage(ReputationHandler.getServerFactions()), ((ServerPlayer)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		}

	}

	//block interaction events
	@SubscribeEvent
	public static void useBlock(PlayerInteractEvent.RightClickBlock event) {
		Level level = event.getWorld();
		if(!level.isClientSide()) {
			ServerLevel serverLevel = (ServerLevel) level;
			if (event.getUseBlock() != Event.Result.DENY && level.getBlockEntity(event.getPos()) instanceof Container) {
				Reputation.logInfo("container");
				if (ContainerHandler.changesReputation(Objects.requireNonNull(level.getBlockEntity(event.getPos())))) {
					for (LivingEntity v : HelperMethods.getSeenEntitiesOfTypeInRange(serverLevel, event.getPlayer().getType(), event.getPos(), 16f)) {
						Villager villager = (Villager) v;
						Reputation.logInfo("Entity: " + v.getName().getString());
						for (Faction faction : ReputationHandler.getEntityFactions(villager)) {
							ReputationHandler.changeReputationStrict(event.getPlayer(), faction, -1 * faction.getActionWeighting("looting"));
						}
					}
				}
			}
		}
	}
	@SubscribeEvent
	public static void placeBlock(BlockEvent.EntityPlaceEvent event) {
		LevelAccessor level = event.getWorld();
		if(event.getEntity() instanceof Player && level.getBlockEntity(event.getPos()) instanceof Container) {
			ContainerHandler.setChangesReputation(Objects.requireNonNull(level.getBlockEntity(event.getPos())), false);
		}
	}

	@SubscribeEvent
	public static void onDeath(LivingDeathEvent event) {
		LivingEntity entity = event.getEntityLiving();
		Level level = entity.level;
		if(!level.isClientSide) {
			DamageSource source = event.getSource();
			Player player = null;
			if (source.getEntity() instanceof Player) player = (Player) source.getEntity();
			else if(source.getDirectEntity() instanceof Player) player = (Player) source.getDirectEntity();
			if (player != null) {
				for(Faction faction : ReputationHandler.getEntityFactions(entity)) {
					for (LivingEntity e : HelperMethods.getSeenEntitiesOfFaction(entity.getBrain(), faction)) {
						ReputationHandler.changeReputationStrict(player, faction, -1 * faction.getActionWeighting("murder"));
						for (Faction enemy : faction.getEnemies()) {
							if (enemy.isMember(e))
								ReputationHandler.changeReputationStrict(player, enemy, faction.getActionWeighting("murder"));
						}
					}
				}
			}
		}
	}

	//attach reputation based goals to AI upon spawning
	@SubscribeEvent
	public static void onJoin(EntityJoinWorldEvent event) {
		if(event.getEntity() instanceof LivingEntity entity) {
			Brain<? extends LivingEntity> brain = entity.getBrain();
			brain.sensors.putIfAbsent(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_LIVING_ENTITIES.create());
			brain.memories.putIfAbsent(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, Optional.empty());
			if (Objects.requireNonNull(ForgeRegistries.ENTITIES.tags()).getTag(TagKeys.FLEE).contains(entity.getType())) {
				ReputationAIPackages.buildReputationSensor(brain);
				ReputationAIPackages.buildReputationFleeAI(brain, HelperMethods.fleeFactor(entity));
				if(entity instanceof Mob mob) mob.goalSelector.addGoal(0, new FleeGoal(mob, HelperMethods.fleeFactor(entity)));
			}
			if (Objects.requireNonNull(ForgeRegistries.ENTITIES.tags()).getTag(TagKeys.HOSTILE).contains(entity.getType()) && entity instanceof Mob mob) {
				ReputationAIPackages.buildReputationSensor(brain);
				ReputationAIPackages.buildReputationHostileAI(mob, brain);
				if(entity instanceof NeutralMob) mob.targetSelector.addGoal(2, new ReputationAttackableTargetGoal<>(mob, Player.class, true, false));
			}
			if (Objects.requireNonNull(ForgeRegistries.ENTITIES.tags()).getTag(TagKeys.PASSIVE_NEUTRAL).contains(entity.getType())) {
				ReputationAIPackages.buildReputationSensor(brain);
				ReputationAIPackages.buildReputationPassiveNeutralAI(brain, 1);
				if(entity instanceof Mob mob) {
					Set<WrappedGoal> goalSet = mob.targetSelector.getAvailableGoals();
					List<WrappedGoal> newGoals = goalSet.stream()
							.filter((g) -> {
								if(g.getGoal() instanceof NearestAttackableTargetGoal targetGoal) return targetGoal.targetType != Player.class && targetGoal.targetType != ServerPlayer.class;
								return false;
							})
							.collect(Collectors.toList());
					mob.targetSelector.removeAllGoals();
					for(WrappedGoal wrappedGoal : newGoals) {
						mob.targetSelector.addGoal(wrappedGoal.getPriority(), wrappedGoal.getGoal());
					}
					mob.targetSelector.addGoal(2, new ReputationPacifyHostileNeutralStandingGoal<>(mob, Player.class, true, false));
				}
			}
			if (Objects.requireNonNull(ForgeRegistries.ENTITIES.tags()).getTag(TagKeys.PASSIVE_GOOD).contains(entity.getType())) {
				ReputationAIPackages.buildReputationSensor(brain);
				ReputationAIPackages.buildReputationPassiveGoodAI(brain, 1);
				if(entity instanceof Mob mob) {
					Set<WrappedGoal> goalSet = mob.targetSelector.getAvailableGoals();
					List<WrappedGoal> newGoals = goalSet.stream()
							.filter((g) -> {
								if(g.getGoal() instanceof NearestAttackableTargetGoal targetGoal && !(g.getGoal() instanceof ReputationPacifyHostileNeutralStandingGoal)) return targetGoal.targetType != Player.class && targetGoal.targetType != ServerPlayer.class;
								return false;
							})
							.collect(Collectors.toList());
					mob.targetSelector.removeAllGoals();
					for(WrappedGoal wrappedGoal : newGoals) {
						mob.targetSelector.addGoal(wrappedGoal.getPriority(), wrappedGoal.getGoal());
					}
					mob.targetSelector.addGoal(2, new ReputationPacifyHostileGoodStandingGoal<>(mob, Player.class, true, false));
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
		for(Faction f : ReputationHandler.getFactions()) {
			respawned.getCapability(ReputationHandler.REPUTATION_CAPABILITY).orElseThrow(RuntimeException::new).setReputation(respawned,f,ReputationHandler.getReputation(original,f));
		}
		original.invalidateCaps();
	}

	//prints the player's reputation for each faction to the screen
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void debugInfo(RenderGameOverlayEvent.Text e) {
		if(ClientConfigHandler.debug.get() && Minecraft.getInstance().player!=null) {
			for (Faction f : ReputationHandler.getFactions()) {
				e.getLeft().add("Reputation for the "+f.getName()+" faction: "+ ReputationHandler.getReputation(Minecraft.getInstance().player,f));
			}
		}
	}
}
