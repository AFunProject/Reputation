package mods.thecomputerizer.reputation.mixin;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.common.ai.ReputationMemoryModule;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerGoalPackages.class)
public abstract class MixinVillagerGoalPackages {

    @Inject(at = @At("HEAD"), method = "getPanicPackage(Lnet/minecraft/world/entity/npc/VillagerProfession;F)Lcom/google/common/collect/ImmutableList;", cancellable = true)
    private static void getPanicPackage(VillagerProfession prof, float p_24603_, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>>> callback) {
        float ff = p_24603_ * 1.5F;
        Reputation.logInfo("Overwrite log check");
        callback.setReturnValue(ImmutableList.of(Pair.of(0, new VillagerCalmDown()), Pair.of(1, SetWalkTargetAwayFrom.entity(MemoryModuleType.NEAREST_HOSTILE, ff, 6, false)), Pair.of(1, SetWalkTargetAwayFrom.entity(MemoryModuleType.HURT_BY_ENTITY, ff, 6, false)), Pair.of(1, SetWalkTargetAwayFrom.entity(ReputationMemoryModule.NEAREST_PLAYER_BAD_REPUTATION.get(), ff*1.5F, 6, false)), Pair.of(3, new VillageBoundRandomStroll(ff, 2, 2)), Pair.of(5, new RunOne<>(ImmutableList.of(Pair.of(new SetEntityLookTarget(EntityType.VILLAGER, 8.0F), 2), Pair.of(new SetEntityLookTarget(EntityType.PLAYER, 8.0F), 2), Pair.of(new DoNothing(30, 60), 8))))));
    }
}
