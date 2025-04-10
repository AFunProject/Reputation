package mods.thecomputerizer.reputation.common.ai;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mods.thecomputerizer.reputation.Reputation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.*;

import static net.minecraftforge.registries.ForgeRegistries.ENTITIES;

public class ReputationStandings {

    public static List<EntityType<?>> PASSIVE_FLEEING_ENTITIES = new ArrayList<>();
    public static List<EntityType<?>> HOSTILE_ENTITIES = new ArrayList<>();
    public static List<EntityType<?>> PASSIVE_ENTITIES = new ArrayList<>();
    public static List<EntityType<?>> INJURED_FLEEING_ENTITIES = new ArrayList<>();
    public static List<EntityType<?>> TRADING_ENTITIES = new ArrayList<>();
    
    private final JsonElement data;
    private final Map<EntityType<?>,String> passive_fleeing_standings;
    private final Map<EntityType<?>,String> hostile_standings;
    private final Map<EntityType<?>,String> passive_standings;
    private final Map<EntityType<?>,String> injured_fleeing_standings;
    private final Map<EntityType<?>,String> trading_standings;

    public ReputationStandings(JsonElement data) {
        this.data = data;
        this.passive_fleeing_standings = new HashMap<>();
        this.hostile_standings = new HashMap<>();
        this.passive_standings = new HashMap<>();
        this.injured_fleeing_standings = new HashMap<>();
        this.trading_standings = new HashMap<>();
        try {
            JsonObject json = data.getAsJsonObject();
            PASSIVE_FLEEING_ENTITIES = parseResourceArray("passive_fleeing",json,"bad",
                    this.passive_fleeing_standings);
            HOSTILE_ENTITIES = parseResourceArray("hostile",json,"bad",this.hostile_standings);
            PASSIVE_ENTITIES = parseResourceArray("passive",json,"good",this.passive_standings);
            INJURED_FLEEING_ENTITIES = parseResourceArray("injured_fleeing",json,"neutral",
                    this.injured_fleeing_standings);
            TRADING_ENTITIES = parseResourceArray("trading",json,"neutral",this.trading_standings);
        } catch(Exception ex) {
            throw new RuntimeException("Failed to parse faction AI!",ex);
        }
    }

    @SuppressWarnings("removal")
    private List<EntityType<?>> parseResourceArray(String element, JsonObject json, String defaultStanding,
            Map<EntityType<?>,String> map) {
        List<EntityType<?>> members = new ArrayList<>();
        if(json.has(element)) {
            for (JsonElement index : json.get(element).getAsJsonArray()) {
                String[] name = index.getAsString().split(":");
                EntityType<?> entity = null;
                if(name.length==1) entity = ENTITIES.getValue(new ResourceLocation(name[0]));
                else if(name.length==2) entity = ENTITIES.getValue(new ResourceLocation(name[0],name[1]));
                else if(name.length==3) {
                    if(checkValidStanding(name[2])) defaultStanding = name[2];
                    entity = ENTITIES.getValue(new ResourceLocation(name[0],name[1]));
                }
                if(Objects.nonNull(entity)) {
                    Reputation.logInfo("Adding attribute to entity {} with custom standing {}",
                                       entity.getRegistryName(),defaultStanding);
                    members.add(entity);
                    map.put(entity,defaultStanding);
                } else Reputation.logError("Could not read standings map for element {}",element);
            }
        }
        return members;
    }

    private static boolean checkValidStanding(String readStanding) {
        return readStanding.matches("bad") || readStanding.matches("neutral") ||
               readStanding.matches("good");
    }

    public JsonElement getData() {
        return this.data;
    }

    public String getPassiveFleeing(EntityType<?> type) {
        this.passive_fleeing_standings.putIfAbsent(type,"bad");
        return this.passive_fleeing_standings.get(type);
    }

    public String getHostile(EntityType<?> type) {
        this.hostile_standings.putIfAbsent(type,"bad");
        return this.hostile_standings.get(type);
    }

    public String getPassive(EntityType<?> type) {
        this.passive_standings.putIfAbsent(type,"good");
        return this.passive_standings.get(type);
    }

    public String getInjured(EntityType<?> type) {
        this.injured_fleeing_standings.putIfAbsent(type,"neutral");
        return this.injured_fleeing_standings.get(type);
    }

    public String getTrading(EntityType<?> type) {
        this.trading_standings.putIfAbsent(type,"neutral");
        return this.trading_standings.get(type);
    }
}