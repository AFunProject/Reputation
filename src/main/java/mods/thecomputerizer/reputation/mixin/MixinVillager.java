package mods.thecomputerizer.reputation.mixin;

import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.util.HelperMethods;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Villager.class)
public abstract class MixinVillager {

    private float reputationTradePriceMultiplier;

    @Inject(at = @At("HEAD"), method = "updateSpecialPrices(Lnet/minecraft/world/entity/player/Player;)V")
    private void updateSpecialPrices(Player player, CallbackInfo callback) {
        if(player.getCapability(ReputationHandler.REPUTATION_CAPABILITY).isPresent()) {
            Villager villager = (Villager)(Object)this;
            for(MerchantOffer merchantoffer : villager.getOffers()) {
                double multiplier = HelperMethods.tradePrice(this.reputationTradePriceMultiplier,merchantoffer.getBaseCostA().getCount(),merchantoffer.getBaseCostA().getMaxStackSize());
                if(multiplier!=0d && multiplier!=1d) {
                    int totalCount = (int) Math.floor(multiplier * (double) merchantoffer.getBaseCostA().getCount());
                    merchantoffer.addToSpecialPriceDiff(totalCount - merchantoffer.getBaseCostA().getCount());
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;", cancellable = true)
    private void mobInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> callback) {
        ItemStack itemstack = player.getItemInHand(hand);
        Villager villager = (Villager)(Object)this;
        if (itemstack.getItem() != Items.VILLAGER_SPAWN_EGG && villager.isAlive() && !villager.isTrading() && !villager.isSleeping() && !player.isSecondaryUseActive()) {
            this.reputationTradePriceMultiplier = HelperMethods.tradeMultiplier(villager,player);
            if(this.reputationTradePriceMultiplier<0.5) {
                villager.setUnhappy();
                callback.setReturnValue(InteractionResult.sidedSuccess(villager.level.isClientSide));
            }
        }
    }
}
