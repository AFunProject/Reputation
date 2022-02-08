package mods.thecomputerizer.reputation.mixin;

import mods.thecomputerizer.reputation.Reputation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.VillagerPanicTrigger;
import mods.thecomputerizer.reputation.common.ai.ReputationMemoryModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerPanicTrigger.class)
public class MixinVillagerPanicTrigger {

    @Inject(at = @At("HEAD"), method = "hasHostile(Lnet/minecraft/world/entity/LivingEntity;)Z")
    private static void hasHostile(LivingEntity villager, CallbackInfoReturnable<Boolean> callback) {
        //float f = 0.5F * 1.5F;
        //villager.getBrain().addActivity(Activity.PANIC, ImmutableList.of(Pair.of(0, SetWalkTargetAwayFrom.entity(ReputationMemoryModule.NEAREST_PLAYER_BAD_REPUTATION.get(), f, 6, false))));
        if(villager.getBrain().hasMemoryValue(ReputationMemoryModule.NEAREST_PLAYER_BAD_REPUTATION.get())) {
            Reputation.logInfo("log");
            callback.setReturnValue(true);
        }
    }
}
