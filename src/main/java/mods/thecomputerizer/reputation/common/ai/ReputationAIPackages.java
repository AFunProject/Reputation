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

import static mods.thecomputerizer.reputation.common.ai.ReputationMemoryModule.FLEE_FROM_PLAYER;
import static mods.thecomputerizer.reputation.common.ai.ReputationMemoryModule.NEAREST_PLAYER_BAD_REPUTATION;
import static mods.thecomputerizer.reputation.common.ai.ReputationMemoryModule.NEAREST_PLAYER_GOOD_REPUTATION;
import static mods.thecomputerizer.reputation.common.ai.ReputationMemoryModule.NEAREST_PLAYER_NEUTRAL_REPUTATION;
import static mods.thecomputerizer.reputation.common.ai.ReputationSenorType.NEAREST_PLAYER_REPUTATION;
import static net.minecraft.world.entity.schedule.Activity.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ReputationAIPackages {

    public static ReputationStandings STANDINGS;

    public static void buildMobLists(JsonElement data) {
        STANDINGS = new ReputationStandings(data);
    }

    public static void buildReputationSensor(Brain<? extends LivingEntity> brain) {
        brain.memories.put(NEAREST_PLAYER_BAD_REPUTATION.get(),Optional.empty());
        brain.memories.put(NEAREST_PLAYER_NEUTRAL_REPUTATION.get(),Optional.empty());
        brain.memories.put(NEAREST_PLAYER_GOOD_REPUTATION.get(),Optional.empty());
        brain.memories.put(FLEE_FROM_PLAYER.get(),Optional.empty());
        brain.sensors.put(NEAREST_PLAYER_REPUTATION.get(),NEAREST_PLAYER_REPUTATION.get().create());
    }

    public static void buildReputationFleeAI(Brain brain, float f, String reputationBound) {
        for(Activity activity : Arrays.asList(PANIC,CORE,MEET,IDLE,REST,HIDE))
            brain.addActivity(activity, getFleePackage(f,reputationBound));
    }

    public static void buildReputationInjuredAI(Brain brain, float f) {
        for(Activity activity : Arrays.asList(PANIC,CORE,MEET,IDLE,REST,HIDE))
            brain.addActivity(activity, getInjuredFleePackage(f));
    }

    public static void buildReputationPassiveAI(Brain brain, int time, String reputationBound) {
        brain.addActivity(CORE,getPassivePackage(time,reputationBound));
        brain.addActivity(FIGHT,getPassivePackage(time,reputationBound));
    }

    public static void buildReputationHostileAI(Mob mob, Brain brain, String reputationBound) {
        brain.addActivity(IDLE,getHostilePackage(mob,brain,reputationBound));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? extends LivingEntity>>> getFleePackage(float f,
            String reputationBound) {
        return ImmutableList.of(Pair.of(0,SetWalkTargetAwayFrom.entity(
                ReputationMemoryModule.getNearestModuleFromString(reputationBound),f,6,false)));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? extends LivingEntity>>> getInjuredFleePackage(
            float f) {
        return ImmutableList.of(Pair.of(0,SetWalkTargetAwayFrom.entity(FLEE_FROM_PLAYER.get(),f,32,false)));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? extends LivingEntity>>> getPassivePackage(int time,
            String reputationBound) {
        return ImmutableList.of(Pair.of(0,new BecomePassiveIfMemoryPresent(
                ReputationMemoryModule.getNearestModuleFromString(reputationBound),time)));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? extends LivingEntity>>> getHostilePackage(Mob mob,
            Brain<? extends LivingEntity> brain, String reputationBound) {
        return ImmutableList.of(Pair.of(1,new StartAttacking<>(attackFunction -> {
            Optional<Player> optional = brain.getMemory(ReputationMemoryModule.getNearestModuleFromString(reputationBound));
            return optional.isPresent() && Sensor.isEntityAttackable(mob,optional.get()) ? optional : Optional.empty();
        })));
    }
}