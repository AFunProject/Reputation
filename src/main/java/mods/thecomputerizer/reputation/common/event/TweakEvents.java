package mods.thecomputerizer.reputation.common.event;

import mods.thecomputerizer.reputation.Constants;
import mods.thecomputerizer.reputation.capability.Faction;
import mods.thecomputerizer.reputation.capability.handlers.ReputationHandler;
import mods.thecomputerizer.reputation.registry.items.FactionCurrencyBag;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.Objects;

@EventBusSubscriber(modid= Constants.MODID)
public class TweakEvents {

	@SubscribeEvent
	public static void onDeath(LivingDeathEvent event) {
		LivingEntity entity = event.getEntityLiving();
		Level level = entity.level;
		if(!level.isClientSide) {
			synchronized (WorldEvents.TRACKER_MAP) {
				WorldEvents.TRACKER_MAP.entrySet().removeIf(entry -> entity.getId()==entry.getKey().getId());
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
			CompoundTag tag = stack.getOrCreateTag();
			if(tag.contains("factionID")) {
				Faction faction = ReputationHandler.getFaction(new ResourceLocation(tag.getString("factionID")));
				if(Objects.nonNull(faction)) {
					tag.putUUID("playerUUID", event.getPlayer().getUUID());
					String itemName = new TranslatableComponent(faction.getCurrencyItem().getDescriptionId()).getString();
					stack.setHoverName(new TranslatableComponent("item.reputation.faction_bad.faction_name",itemName));
				}
			}
		}
	}
}
