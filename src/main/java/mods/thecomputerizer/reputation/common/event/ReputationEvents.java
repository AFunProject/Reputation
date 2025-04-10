package mods.thecomputerizer.reputation.common.event;

import mods.thecomputerizer.reputation.ReputationRef;
import mods.thecomputerizer.reputation.capability.Faction;
import mods.thecomputerizer.reputation.capability.handlers.ContainerHandler;
import mods.thecomputerizer.reputation.capability.handlers.ReputationHandler;
import mods.thecomputerizer.reputation.capability.placedcontainer.PlacedContainerProvider;
import mods.thecomputerizer.reputation.capability.reputation.ReputationProvider;
import mods.thecomputerizer.reputation.common.ai.ChatTracker;
import mods.thecomputerizer.reputation.common.ai.ServerTrackers;
import mods.thecomputerizer.reputation.util.HelperMethods;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static mods.thecomputerizer.reputation.ReputationRef.MODID;
import static mods.thecomputerizer.reputation.capability.placedcontainer.PlacedContainerProvider.PLACED_CONTAINER_CAPABILITY;
import static mods.thecomputerizer.reputation.common.event.WorldEvents.TRACKER_MAP;
import static net.minecraft.world.entity.EntityType.VILLAGER;
import static net.minecraftforge.eventbus.api.Event.Result.DENY;
import static net.minecraftforge.eventbus.api.EventPriority.LOWEST;

@EventBusSubscriber(modid=MODID)
public class ReputationEvents {

	public static final List<LivingEntity> TICK_THESE = new ArrayList<>();

	@SubscribeEvent
	public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if(entity instanceof Player && !(entity instanceof FakePlayer))
			event.addCapability(ReputationRef.res("reputation"), new ReputationProvider());
	}

	@SubscribeEvent
	public static void attachBlockCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
		BlockEntity block = event.getObject();
		if (block instanceof LidBlockEntity || block instanceof BarrelBlockEntity)
			event.addCapability(ReputationRef.res("container_placed"), new PlacedContainerProvider());
	}

	@SubscribeEvent
	public static void useBlock(PlayerInteractEvent.RightClickBlock event) {
		if(event.getWorld() instanceof ServerLevel level && event.getUseBlock()!=DENY) {
			BlockEntity entity = level.getBlockEntity(event.getPos());
			if(entity instanceof Container &&
					entity.getCapability(PLACED_CONTAINER_CAPABILITY).isPresent() &&
					ContainerHandler.changesReputation(entity)) {
				for(LivingEntity living : HelperMethods.getSeenEntitiesOfTypeInRange(level,event.getPlayer(),
						VILLAGER,event.getPos(),16f)) {
					Villager villager = (Villager)living;
					for(Faction faction : ReputationHandler.getEntityFactions(villager))
						ReputationHandler.changeReputation(event.getPlayer(),faction,
								-1*faction.getActionWeighting("looting"));
				}
			}
		}
	}

	@SubscribeEvent
	public static void placeBlock(EntityPlaceEvent event) {
		LevelAccessor level = event.getWorld();
		if(event.getEntity() instanceof Player) {
			BlockEntity entity = level.getBlockEntity(event.getPos());
			if(entity instanceof LidBlockEntity || entity instanceof BarrelBlockEntity)
				ContainerHandler.setChangesReputation(entity,false);
		}
	}

	@SubscribeEvent(priority=LOWEST)
	public static void setTarget(LivingChangeTargetEvent event) {
		if(!event.isCanceled()) {
			LivingEntity entity = event.getEntityLiving();
			synchronized(TRACKER_MAP) {
				if(TRACKER_MAP.containsKey(entity)) {
					ChatTracker tracker = TRACKER_MAP.get(entity);
					if(tracker.notRecent() && !tracker.getEngage() &&
					   ServerTrackers.hasIconsForEvent(tracker.getEntityType(),"engage")) {
						tracker.setEngage(true);
						tracker.setChanged(true);
						tracker.setRecent(true);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onDeath(LivingDeathEvent event) {
		LivingEntity entity = event.getEntityLiving();
		if(entity.level instanceof ServerLevel level) {
			TICK_THESE.remove(entity);
			DamageSource source = event.getSource();
			Player player = null;
			if(source.getEntity() instanceof Player) player = (Player)source.getEntity();
			else if(source.getDirectEntity() instanceof Player) player = (Player)source.getDirectEntity();
			if(Objects.nonNull(player)) {
				for(Faction faction : ReputationHandler.getEntityFactions(entity)) {
					for(LivingEntity e : HelperMethods.getSeenEntitiesOfFaction(level,entity,16,entity.getBrain(),faction)) {
						ReputationHandler.changeReputation(player,faction,-1*faction.getActionWeighting("murder"));
						for(Faction enemy : faction.getEnemies())
							if(enemy.isMember(e))
								ReputationHandler.changeReputation(player,enemy,faction.getActionWeighting("murder"));
					}
				}
			}
		}
	}
}