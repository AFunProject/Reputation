package mods.thecomputerizer.reputation.common.event;

import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import mods.thecomputerizer.reputation.common.objects.items.FactionCurrencyBag;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.Objects;

@SuppressWarnings("deprecation")
@EventBusSubscriber(modid=ModDefinitions.MODID)
public class TweakEvents {

	@SubscribeEvent
	public static void livingUpdate(LivingEvent.LivingUpdateEvent event) {
		LivingEntity entity = event.getEntityLiving();
		Level level = entity.level;
		if(!level.isClientSide) {
			if (entity instanceof AbstractSkeleton &! entity.isOnFire() &! entity.fireImmune()
					&& level.dimensionType().ultraWarm()) {
				entity.setRemainingFireTicks(100);
			}
		}
	}

	@SubscribeEvent
	public static void onDeath(LivingDeathEvent event) {
		LivingEntity entity = event.getEntityLiving();
		Level level = entity.level;
		if(!level.isClientSide) {
			if (event.getSource()==DamageSource.ON_FIRE && entity instanceof AbstractSkeleton && level.getBiome(entity.blockPosition()).unwrapKey().isPresent() &&
					BiomeDictionary.hasType(level.getBiome(entity.blockPosition()).unwrapKey().get(), BiomeDictionary.Type.HOT)) {
				BlockPos pos = entity.blockPosition();
				WitherSkeleton newentity = new WitherSkeleton(EntityType.WITHER_SKELETON, level);
				newentity.setPos(pos.getX(), pos.getY(), pos.getZ());
				for (EquipmentSlot slot : EquipmentSlot.values()) {
					newentity.setItemSlot(slot, entity.getItemBySlot(slot));
				}
				if (entity.hasCustomName()) newentity.setCustomName(entity.getCustomName());
				level.addFreshEntity(newentity);
			}
			LivingEntity toRemove = null;
			synchronized (WorldEvents.TRACKER_MAP) {
				for (LivingEntity living : WorldEvents.TRACKER_MAP.keySet())
					if (entity.getId() == WorldEvents.TRACKER_MAP.get(living).getEntityID()) toRemove = living;
				if (toRemove != null) WorldEvents.TRACKER_MAP.remove(toRemove);
			}
		}
	}

	@SubscribeEvent
	public static void arrowCollide(ProjectileImpactEvent event) {
		Projectile entity = event.getProjectile();
		Level level = entity.level;
		if(level.isClientSide) {
			if(entity.isAddedToWorld()) {
				if (event.getEntity().isOnFire()) {
					HitResult hit = event.getRayTraceResult();
					if (hit instanceof BlockHitResult blockHit) {
						BlockPos pos = blockHit.getBlockPos().relative(blockHit.getDirection());
						if (level.getBlockState(pos).isAir()) {
							level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onCraft(PlayerEvent.ItemCraftedEvent event) {
		ItemStack stack = event.getCrafting();
		if(stack.getItem() instanceof FactionCurrencyBag) {
			if (event.getCrafting().hasTag()) {
				CompoundTag tag = event.getCrafting().getOrCreateTag();
				if(tag.contains("factionID")) {
					Faction faction = ReputationHandler.getFaction(new ResourceLocation(tag.getString("factionID")));
					if(Objects.nonNull(faction)) {
						tag.putUUID("playerUUID",event.getPlayer().getUUID());
						stack.setHoverName(new TextComponent("Bag of "+
								new TranslatableComponent(faction.getCurrencyItem().getDescriptionId()).getString()));
					}
				}
			}
		}
	}
}
