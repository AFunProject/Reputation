package mods.thecomputerizer.reputation.common.event;

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
import net.minecraft.world.item.Item;
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
import net.minecraftforge.registries.ForgeRegistries;

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

	@SubscribeEvent
	public static void onCraft(PlayerEvent.ItemCraftedEvent event) {
		ItemStack stack = event.getCrafting();
		if(stack.getItem() instanceof FactionCurrencyBag) {
			if (event.getCrafting().hasTag()) {
				CompoundTag nbt = event.getCrafting().getTag();
				if(nbt.contains("item") && nbt.getCompound("item").contains("id")) {
					Item craftedWith = ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getCompound("item").get("id").getAsString()));
					if(craftedWith!=null) stack.setHoverName(new TextComponent("Bag of "+new TranslatableComponent(craftedWith.getDescriptionId()).getString()));
				}
			}
		}
	}
}
