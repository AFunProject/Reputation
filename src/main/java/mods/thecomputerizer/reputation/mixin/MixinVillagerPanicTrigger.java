package mods.thecomputerizer.reputation.mixin;

import mods.thecomputerizer.reputation.Reputation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.VillagerPanicTrigger;
import mods.thecomputerizer.reputation.common.ai.ReputationMemoryModule;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerPanicTrigger.class)
public class MixinVillagerPanicTrigger {

    @Inject(at = @At("RETURN"), method = "hasHostile(Lnet/minecraft/world/entity/LivingEntity;)Z", cancellable = true)
    private static void hasHostile(LivingEntity villager, CallbackInfoReturnable<Boolean> callback) {
        if(villager.getBrain().hasMemoryValue(ReputationMemoryModule.NEAREST_PLAYER_BAD_REPUTATION.get())) {
            callback.setReturnValue(true);
        }
    }
}
