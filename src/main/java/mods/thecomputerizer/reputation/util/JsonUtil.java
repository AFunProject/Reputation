package mods.thecomputerizer.reputation.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class JsonUtil {

    public static Optional<EntityType<?>> potentialEntity(JsonObject json, String element) {
        if(Objects.isNull(json) || !json.has(element)) return Optional.empty();
        try {
            ResourceLocation resource = new ResourceLocation(json.get(element).getAsString());
            return ForgeRegistries.ENTITIES.containsKey(resource) ? Optional.ofNullable(ForgeRegistries.ENTITIES.getValue(resource)) : Optional.empty();
        } catch (Exception e) {
            Reputation.logError("Could not read json element {}",element,e);
            return Optional.empty();
        }
    }

    /**
     * Return an empty optional if the json object is null or if none of the elements exist within it
     */
    public static Map<String, List<ResourceLocation>> potentialResourceMap(
            JsonObject json, boolean distinctValues, String resourcePath, String ... elements) {
        Map<String, List<ResourceLocation>> ret = new HashMap<>();
        for(String element : elements) {
            List<ResourceLocation> potentialResources = potentialResourceList(json,resourcePath,element,distinctValues);
            if(!potentialResources.isEmpty())
                ret.put(element,potentialResources);
        }
        return ret;
    }

    /**
     * Returns an empty list if the json object is null or something fails to parse into a resource
     */
    public static List<ResourceLocation> potentialResourceList(
            JsonObject json, String resourcePath, String element, boolean distinct) {
        if(Objects.nonNull(json) && json.has(element)) {
            try {
                List<ResourceLocation> resources = new ArrayList<>();
                for(JsonElement e : json.get(element).getAsJsonArray()) {
                    ResourceLocation resource = Constants.res(resourcePath.replaceFirst("\\{}",e.getAsString()));
                    if(!distinct || !resources.contains(resource)) resources.add(resource);
                }
                return resources;
            } catch (Exception ex) {
                Reputation.logError("Failed to read resource from json element {}",json.get(element).getAsString(),ex);
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    public static OptionalLong potentialLong(JsonObject json, String element) {
        try {
            return Objects.nonNull(json) && json.has(element) ?
                    OptionalLong.of(json.get(element).getAsLong()) : OptionalLong.empty();
        } catch (Exception e) {
            Reputation.logError("Could not read json element {}",element,e);
            return OptionalLong.empty();
        }
    }

    public static Optional<Float> potentialFloat(JsonObject json, String element) {
        try {
            return Objects.nonNull(json) && json.has(element) ?
                    Optional.of(json.get(element).getAsFloat()) : Optional.empty();
        } catch (Exception e) {
            Reputation.logError("Could not read json element {}",element,e);
            return Optional.empty();
        }
    }
}
