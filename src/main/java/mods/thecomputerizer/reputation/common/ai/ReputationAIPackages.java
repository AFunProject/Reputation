package mods.thecomputerizer.reputation.common.ai;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetAwayFrom;
import net.minecraft.world.entity.ai.behavior.VillageBoundRandomStroll;
import net.minecraft.world.entity.ai.behavior.VillagerCalmDown;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.VillagerProfession;

public class ReputationAIPackages {

    //public static ImmutableList<Pair<Integer, ? extends Behavior<? super LivingEntity>>> getFleePackage(float num) {
        //float f = num * 1.5F;
        //return ImmutableList.of(Pair.of(0, new VillagerCalmDown()), Pair.of(1, SetWalkTargetAwayFrom.entity(MemoryModuleType.NEAREST_HOSTILE, f, 6, false)), Pair.of(1, SetWalkTargetAwayFrom.entity(MemoryModuleType.HURT_BY_ENTITY, f, 6, false)), Pair.of(3, new VillageBoundRandomStroll(f, 2, 2)), getMinimalLookBehavior());
    //}
}
