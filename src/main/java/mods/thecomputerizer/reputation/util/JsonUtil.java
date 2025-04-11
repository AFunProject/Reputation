package mods.thecomputerizer.reputation.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.ReputationRef;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.*;

import static net.minecraftforge.registries.ForgeRegistries.ENTITIES;

public class JsonUtil {
    
    @SuppressWarnings("removal")
    private static ResourceLocation asResource(String resourceLocation) {
        return new ResourceLocation(resourceLocation);
    }

    public static Optional<EntityType<?>> potentialEntity(JsonObject json, String element) {
        if(Objects.isNull(json) || !json.has(element)) return Optional.empty();
        try {
            ResourceLocation resource = asResource(json.get(element).getAsString());
            return ENTITIES.containsKey(resource) ? Optional.ofNullable(ENTITIES.getValue(resource)) : Optional.empty();
        } catch(Exception ex) {
            Reputation.logError("Could not read json element {}",element,ex);
        }
        return Optional.empty();
    }

    /**
     * Return an empty optional if the JSON object is null or if none of the elements exist within it
     */
    public static Map<String, List<ResourceLocation>> potentialResourceMap(JsonObject json, boolean distinctValues,
            String resourcePath, String ... elements) {
        Map<String,List<ResourceLocation>> ret = new HashMap<>();
        for(String element : elements) {
            List<ResourceLocation> potentialResources = potentialResourceList(json,resourcePath,element,distinctValues);
            if(!potentialResources.isEmpty()) ret.put(element,potentialResources);
        }
        return ret;
    }

    /**
     * Returns an empty list if the JSON object is null or something fails to parse into a resource
     */
    public static List<ResourceLocation> potentialResourceList(
            JsonObject json, String resourcePath, String element, boolean distinct) {
        if(Objects.nonNull(json) && json.has(element)) {
            try {
                List<ResourceLocation> resources = new ArrayList<>();
                for(JsonElement e : json.get(element).getAsJsonArray()) {
                    String path = resourcePath.replaceFirst("\\{}",e.getAsString());
                    ResourceLocation resource = ReputationRef.res(path);
                    if(!distinct || !resources.contains(resource)) resources.add(resource);
                }
                return resources;
            } catch(Exception ex) {
                Reputation.logError("Failed to read resource from json element {}",
                                    json.get(element).getAsString(),ex);
            }
        }
        return List.of();
    }

    public static OptionalLong potentialLong(JsonObject json, String element) {
        try {
            return Objects.nonNull(json) && json.has(element) ?
                    OptionalLong.of(json.get(element).getAsLong()) : OptionalLong.empty();
        } catch(Exception ex) {
            Reputation.logError("Could not read json element {}",element,ex);
        }
        return OptionalLong.empty();
    }

    public static Optional<Float> potentialFloat(JsonObject json, String element) {
        try {
            return Objects.nonNull(json) && json.has(element) ?
                    Optional.of(json.get(element).getAsFloat()) : Optional.empty();
        } catch(Exception ex) {
            Reputation.logError("Could not read json element {}",element,ex);
        }
        return Optional.empty();
    }
}