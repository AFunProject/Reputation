package mods.thecomputerizer.reputation.common.ai;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
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

    public static ReputationStandings STANDINGS;

    public static void buildMobLists(JsonElement data) {
        STANDINGS = new ReputationStandings(data);
    }

    public static void buildReputationSensor(Brain<? extends LivingEntity> brain) {
        brain.memories.put(ReputationMemoryModule.NEAREST_PLAYER_BAD_REPUTATION.get(),Optional.empty());
        brain.memories.put(ReputationMemoryModule.NEAREST_PLAYER_NEUTRAL_REPUTATION.get(),Optional.empty());
        brain.memories.put(ReputationMemoryModule.NEAREST_PLAYER_GOOD_REPUTATION.get(),Optional.empty());
        brain.memories.put(ReputationMemoryModule.FLEE_FROM_PLAYER.get(),Optional.empty());
        brain.sensors.put(ReputationSenorType.NEAREST_PLAYER_REPUTATION.get(),ReputationSenorType.NEAREST_PLAYER_REPUTATION.get().create());
    }

    public static void buildReputationFleeAI(Brain brain, float f, String reputationBound) {
        for (Activity activity : Arrays.asList(Activity.PANIC,Activity.CORE,Activity.MEET,Activity.IDLE,Activity.REST,Activity.HIDE)) {
            brain.addActivity(activity, getFleePackage(f,reputationBound));
        }
    }

    public static void buildReputationInjuredAI(Brain brain, float f) {
        for (Activity activity : Arrays.asList(Activity.PANIC,Activity.CORE,Activity.MEET,Activity.IDLE,Activity.REST,Activity.HIDE)) {
            brain.addActivity(activity, getInjuredFleePackage(f));
        }
    }

    public static void buildReputationPassiveAI(Brain brain, int time, String reputationBound) {
        brain.addActivity(Activity.CORE,getPassivePackage(time,reputationBound));
        brain.addActivity(Activity.FIGHT,getPassivePackage(time,reputationBound));
    }

    public static void buildReputationHostileAI(Mob mob, Brain brain, String reputationBound) {
        brain.addActivity(Activity.IDLE,getHostilePackage(mob,brain,reputationBound));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? extends LivingEntity>>> getFleePackage(float f, String reputationBound) {
        return ImmutableList.of(Pair.of(0,SetWalkTargetAwayFrom.entity(ReputationMemoryModule.getNearestModuleFromString(reputationBound),f,6,false)));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? extends LivingEntity>>> getInjuredFleePackage(float f) {
        return ImmutableList.of(Pair.of(0,SetWalkTargetAwayFrom.entity(ReputationMemoryModule.FLEE_FROM_PLAYER.get(),f,32,false)));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? extends LivingEntity>>> getPassivePackage(int time, String reputationBound) {
        return ImmutableList.of(Pair.of(0,new BecomePassiveIfMemoryPresent(ReputationMemoryModule.getNearestModuleFromString(reputationBound),time)));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? extends LivingEntity>>> getHostilePackage(Mob mob, Brain<? extends LivingEntity> brain, String reputationBound) {
        return ImmutableList.of(Pair.of(1, new StartAttacking<>(attackFunction -> {
            Optional<Player> optional = brain.getMemory(ReputationMemoryModule.getNearestModuleFromString(reputationBound));
            return optional.isPresent() && Sensor.isEntityAttackable(mob, optional.get()) ? optional : Optional.empty();
        })));
    }
}
