package mods.thecomputerizer.reputation.mixin;

import mods.thecomputerizer.reputation.capability.reputation.ReputationProvider;
import mods.thecomputerizer.reputation.util.HelperMethods;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class MixinVillager {

    @Inject(at = @At("HEAD"), method = "startTrading(Lnet/minecraft/world/entity/player/Player;)V", cancellable = true)
    private void reputation$startTrading(Player player, CallbackInfo ci) {
        if(player.getCapability(ReputationProvider.REPUTATION_CAPABILITY).isPresent()) {
            Villager villager = (Villager) (Object) this;
            float priceMultiplier = HelperMethods.tradeMultiplier(villager, player);
            if (priceMultiplier<0.5f) {
                villager.setUnhappy();
                ci.cancel();
            }
            else {
                for(MerchantOffer merchantoffer : villager.getOffers()) {
                    ItemStack stackA = merchantoffer.getBaseCostA();
                    double multiplier = HelperMethods.tradePrice(priceMultiplier,stackA.getCount(),stackA.getMaxStackSize());
                    if(multiplier!=0d && multiplier!=1d) {
                        int totalCount = (int)Math.floor(multiplier*(double)stackA.getCount());
                        merchantoffer.addToSpecialPriceDiff(totalCount-stackA.getCount());
                    }
                }
            }
        }
    }
}
