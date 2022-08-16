package mods.thecomputerizer.reputation.common.ai;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
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
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ReputationAIPackages {

    public static HashMap<EntityType<?>, String> passive_fleeing_standings = new HashMap<>();
    public static HashMap<EntityType<?>, String> hostile_standings = new HashMap<>();
    public static HashMap<EntityType<?>, String> passive_standings = new HashMap<>();
    public static HashMap<EntityType<?>, String> injured_fleeing_standings = new HashMap<>();
    public static HashMap<EntityType<?>, String> trading_standings = new HashMap<>();

    public static void buildMobLists(JsonElement data) {
        try {
            JsonObject json = data.getAsJsonObject();
            ModDefinitions.PASSIVE_FLEEING_ENTITIES = parseResourceArray("passive_fleeing",json,"bad");
            ModDefinitions.HOSTILE_ENTITIES = parseResourceArray("hostile",json,"bad");
            ModDefinitions.PASSIVE_ENTITIES = parseResourceArray("passive",json,"good");
            ModDefinitions.INJURED_FLEEING_ENTITIES = parseResourceArray("injured_fleeing",json,"neutral");
            ModDefinitions.TRADING_ENTITIES = parseResourceArray("trading",json,"neutral");
            parseResourceArray("hostile_fleeing",json,"neutral");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to parse faction ai!");
        }
    }

    private static List<EntityType<?>> parseResourceArray(String element, JsonObject json, String defaultStanding) {
        List<EntityType<?>> members = new ArrayList<>();
        if(json.has(element)) {
            for (JsonElement index : json.get(element).getAsJsonArray()) {
                String[] name = index.getAsString().split(":");
                EntityType<?> entity = null;
                if(name.length==1) entity = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(name[0]));
                else if(name.length==2) entity = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(name[0],name[1]));
                else if(name.length==3) {
                    if(checkValidStanding(name[0])) defaultStanding = name[0];
                    entity = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(name[1],name[2]));
                }
                if(entity!=null) {
                    members.add(entity);
                    try {
                        ((HashMap<EntityType<?>, String>)ReputationAIPackages.class.getField(element+"_standings").get(null)).put(entity,defaultStanding);
                    } catch (Exception e) {
                        Reputation.logError("Could not read standings map for element: "+element,e);
                    }
                } else Reputation.logError("Could not read standings map for element: "+element,null);
            }
        }
        return members;
    }

    private static boolean checkValidStanding(String readStanding) {
        return readStanding.matches("bad") || readStanding.matches("neutral") || readStanding.matches("good");
    }

    public static void buildReputationSensor(Brain<? extends LivingEntity> brain) {
        brain.memories.put(ReputationMemoryModule.NEAREST_PLAYER_BAD_REPUTATION.get(), Optional.empty());
        brain.memories.put(ReputationMemoryModule.NEAREST_PLAYER_NEUTRAL_REPUTATION.get(), Optional.empty());
        brain.memories.put(ReputationMemoryModule.NEAREST_PLAYER_GOOD_REPUTATION.get(), Optional.empty());
        brain.memories.put(ReputationMemoryModule.FLEE_FROM_PLAYER.get(), Optional.empty());
        brain.sensors.put(ReputationSenorType.NEAREST_PLAYER_REPUTATION.get(), ReputationSenorType.NEAREST_PLAYER_REPUTATION.get().create());
    }

    public static void buildReputationFleeAI(Brain brain, float f, String reputationBound) {
        for (Activity activity : Arrays.asList(Activity.PANIC, Activity.CORE, Activity.MEET, Activity.IDLE, Activity.REST, Activity.HIDE)) {
            brain.addActivity(activity, getFleePackage(f,reputationBound));
        }
    }

    public static void buildReputationInjuredAI(Brain brain, float f) {
        for (Activity activity : Arrays.asList(Activity.PANIC, Activity.CORE, Activity.MEET, Activity.IDLE, Activity.REST, Activity.HIDE)) {
            brain.addActivity(activity, getInjuredFleePackage(f));
        }
    }

    public static void buildReputationPassiveAI(Brain brain, int time, String reputationBound) {
        brain.addActivity(Activity.CORE, getPassivePackage(time,reputationBound));
        brain.addActivity(Activity.FIGHT, getPassivePackage(time,reputationBound));
    }

    public static void buildReputationHostileAI(Mob mob, Brain brain, String reputationBound) {
        brain.addActivity(Activity.IDLE, getHostilePackage(mob, brain, reputationBound));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? extends LivingEntity>>> getFleePackage(float f, String reputationBound) {
        return ImmutableList.of(Pair.of(0, SetWalkTargetAwayFrom.entity(ReputationMemoryModule.getNearestModuleFromString(reputationBound), f, 6, false)));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? extends LivingEntity>>> getInjuredFleePackage(float f) {
        return ImmutableList.of(Pair.of(0, SetWalkTargetAwayFrom.entity(ReputationMemoryModule.FLEE_FROM_PLAYER.get(), f, 32, false)));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? extends LivingEntity>>> getPassivePackage(int time, String reputationBound) {
        return ImmutableList.of(Pair.of(0, new BecomePassiveIfMemoryPresent(ReputationMemoryModule.getNearestModuleFromString(reputationBound),time)));
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? extends LivingEntity>>> getHostilePackage(Mob mob, Brain<? extends LivingEntity> brain, String reputationBound) {
        return ImmutableList.of(Pair.of(1, new StartAttacking<>(attackFunction -> {
            Optional<Player> optional = brain.getMemory(ReputationMemoryModule.getNearestModuleFromString(reputationBound));
            return optional.isPresent() && Sensor.isEntityAttackable(mob, optional.get()) ? optional : Optional.empty();
        })));
    }
}
