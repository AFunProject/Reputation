package mods.thecomputerizer.reputation.common.ai;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReputationStandings {

    private final HashMap<EntityType<?>, String> passive_fleeing_standings ;
    private final HashMap<EntityType<?>, String> hostile_standings;
    private final HashMap<EntityType<?>, String> passive_standings;
    private final HashMap<EntityType<?>, String> injured_fleeing_standings;
    private final HashMap<EntityType<?>, String> trading_standings;


    public ReputationStandings(JsonElement data) {
        this.passive_fleeing_standings = new HashMap<>();
        this.hostile_standings = new HashMap<>();
        this.passive_standings = new HashMap<>();
        this.injured_fleeing_standings = new HashMap<>();
        this.trading_standings = new HashMap<>();
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

    private List<EntityType<?>> parseResourceArray(String element, JsonObject json, String defaultStanding) {
        List<EntityType<?>> members = new ArrayList<>();
        if(json.has(element)) {
            for (JsonElement index : json.get(element).getAsJsonArray()) {
                String[] name = index.getAsString().split(":");
                EntityType<?> entity = null;
                if(name.length==1) entity = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(name[0]));
                else if(name.length==2) entity = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(name[0],name[1]));
                else if(name.length==3) {
                    if(element.matches("trading"))
                        Reputation.logInfo(name[0]+" "+name[1]+" "+name[2]);
                    if(checkValidStanding(name[0])) defaultStanding = name[0];
                    entity = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(name[0],name[1]));
                }
                if(entity!=null) {
                    members.add(entity);
                    try {
                        Field field = this.getClass().getField(element+"_standings");
                        ((HashMap<EntityType<?>, String>)field.get(this)).put(entity,defaultStanding);
                        if(element.matches("trading"))
                            Reputation.logInfo(((HashMap<EntityType<?>, String>)field.get(this)).get(entity));
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
}
