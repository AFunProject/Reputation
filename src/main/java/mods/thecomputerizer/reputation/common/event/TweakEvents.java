package mods.thecomputerizer.reputation.common.event;

import mods.thecomputerizer.reputation.common.ModDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

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
			for(LivingEntity living : WorldEvents.trackers.keySet()) if(entity.getUUID()==WorldEvents.trackers.get(living).getEntityUUID()) toRemove = living;
			if(toRemove!=null) WorldEvents.trackers.remove(toRemove);
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

}
