package mods.thecomputerizer.reputation.common.ai;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BecomePassiveIfMemoryPresent;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetAwayFrom;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Arrays;
import java.util.Optional;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ReputationAIPackages {

    public static void buildReputationSensor(Brain<? extends LivingEntity> brain) {
        brain.sensors.put(ReputationSenorType.NEAREST_PLAYER_REPUTATION.get(), ReputationSenorType.NEAREST_PLAYER_REPUTATION.get().create());
        brain.memories.put(ReputationMemoryModule.NEAREST_PLAYER_BAD_REPUTATION.get(), Optional.empty());
        brain.memories.put(ReputationMemoryModule.NEAREST_PLAYER_NEUTRAL_REPUTATION.get(), Optional.empty());
        brain.memories.put(ReputationMemoryModule.NEAREST_PLAYER_GOOD_REPUTATION.get(), Optional.empty());
        brain.memories.put(ReputationMemoryModule.FLEE_FROM_PLAYER.get(), Optional.empty());
    }

    public static void buildReputationFleeAI(Brain brain, float f) {
        for (Activity activity : Arrays.asList(Activity.PANIC, Activity.CORE, Activity.MEET, Activity.IDLE, Activity.REST, Activity.HIDE)) {
            brain.addActivity(activity, getFleePackage(f));
        }
    }

    public static void buildReputationInjuredAI(Brain brain, float f) {
        for (Activity activity : Arrays.asList(Activity.PANIC, Activity.CORE, Activity.MEET, Activity.IDLE, Activity.REST, Activity.HIDE)) {
            brain.addActivity(activity, getInjuredFleePackage(f));
        }
    }

    public static void buildReputationPassiveNeutralAI(Brain brain, int time) {
        brain.addActivity(Activity.CORE, getPassiveNeutralPackage(time));
        brain.addActivity(Activity.FIGHT, getPassiveNeutralPackage(time));
    }

    public static void buildReputationPassiveGoodAI(Brain brain, int time) {
        brain.addActivity(Activity.CORE, getPassiveGoodPackage(time));
        brain.addActivity(Activity.FIGHT, getPassiveGoodPackage(time));
    }

    public static void buildReputationHostileAI(Mob mob, Brain brain) {
        brain.addActivity(Activity.IDLE, getHostilePackage(mob, brain));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? extends LivingEntity>>> getFleePackage(float f) {
        return ImmutableList.of(Pair.of(0, SetWalkTargetAwayFrom.entity(ReputationMemoryModule.NEAREST_PLAYER_BAD_REPUTATION.get(), f, 6, false)));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? extends LivingEntity>>> getInjuredFleePackage(float f) {
        return ImmutableList.of(Pair.of(0, SetWalkTargetAwayFrom.entity(ReputationMemoryModule.FLEE_FROM_PLAYER.get(), f, 32, false)));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? extends LivingEntity>>> getPassiveNeutralPackage(int time) {
        return ImmutableList.of(Pair.of(0, new BecomePassiveIfMemoryPresent(ReputationMemoryModule.NEAREST_PLAYER_NEUTRAL_REPUTATION.get(),time)));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? extends LivingEntity>>> getPassiveGoodPackage(int time) {
        return ImmutableList.of(Pair.of(0, new BecomePassiveIfMemoryPresent(ReputationMemoryModule.NEAREST_PLAYER_GOOD_REPUTATION.get(),time)));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? extends LivingEntity>>> getHostilePackage(Mob mob, Brain<? extends LivingEntity> brain) {
        return ImmutableList.of(Pair.of(1, new StartAttacking<>(attackFunction -> {
            Optional<Player> optional = brain.getMemory(ReputationMemoryModule.NEAREST_PLAYER_BAD_REPUTATION.get());
            return optional.isPresent() && Sensor.isEntityAttackable(mob, optional.get()) ? optional : Optional.empty();
        })));
    }
}
