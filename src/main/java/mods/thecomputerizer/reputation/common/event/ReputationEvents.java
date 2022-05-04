package mods.thecomputerizer.reputation.common.event;

import mods.thecomputerizer.reputation.api.ContainerHandler;
import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import mods.thecomputerizer.reputation.common.capability.PlacedContainerProvider;
import mods.thecomputerizer.reputation.common.capability.ReputationProvider;
import mods.thecomputerizer.reputation.util.HelperMethods;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.Objects;

@EventBusSubscriber(modid = ModDefinitions.MODID)
public class ReputationEvents {

	@SubscribeEvent
	public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (entity instanceof Player & !(entity instanceof FakePlayer)) {
			event.addCapability(ModDefinitions.getResource("reputation"), new ReputationProvider());
		}
	}

	@SubscribeEvent
	public static void attachBlockCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
		BlockEntity block = event.getObject();
		if (block instanceof Container) {
			event.addCapability(ModDefinitions.getResource("container_placed"), new PlacedContainerProvider());
		}
	}

	//block interaction events
	@SubscribeEvent
	public static void useBlock(PlayerInteractEvent.RightClickBlock event) {
		Level level = event.getWorld();
		if(!level.isClientSide()) {
			ServerLevel serverLevel = (ServerLevel) level;
			if (event.getUseBlock() != Event.Result.DENY && level.getBlockEntity(event.getPos()) instanceof Container) {
				if(Objects.requireNonNull(level.getBlockEntity(event.getPos())).getCapability(PlacedContainerProvider.PLACED_CONTAINER_CAPABILITY).isPresent()) {
					if (ContainerHandler.changesReputation(Objects.requireNonNull(level.getBlockEntity(event.getPos())))) {
						for (LivingEntity v : HelperMethods.getSeenEntitiesOfTypeInRange(serverLevel, event.getPlayer(), EntityType.VILLAGER, event.getPos(), 16f)) {
							Villager villager = (Villager) v;
							for (Faction faction : ReputationHandler.getEntityFactions(villager)) {
								ReputationHandler.changeReputation(event.getPlayer(), faction, -1 * faction.getActionWeighting("looting"));
							}
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
						ReputationHandler.changeReputation(player, faction, -1 * faction.getActionWeighting("murder"));
						for (Faction enemy : faction.getEnemies()) {
							if (enemy.isMember(e))
								ReputationHandler.changeReputation(player, enemy, faction.getActionWeighting("murder"));
						}
					}
				}
			}
		}
	}
}
