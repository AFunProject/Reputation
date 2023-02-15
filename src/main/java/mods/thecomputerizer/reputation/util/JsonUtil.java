package mods.thecomputerizer.reputation.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JsonUtil {

    public static Optional<EntityType<?>> potentialEntity(JsonObject json, String element) {
        if(Objects.isNull(json) || !json.has(element)) return Optional.empty();
        try {
            ResourceLocation resource = new ResourceLocation(json.get(element).getAsString());
            EntityType<?> type = ForgeRegistries.ENTITIES.getValue(resource);
            return Objects.nonNull(type) ? Optional.of(type) : Optional.empty();
        } catch (Exception e) {
            Reputation.logError("Could not read json element {}",element,e);
            return Optional.empty();
        }
    }

    //return an empty optional if the json object is null or if none of the elements exist within it
    public static Map<String, List<ResourceLocation>> potentialResourceMap(JsonObject json, boolean distinctValues,
                                                                                     String resourcePath, String ... elements) {
        if(Objects.isNull(json)) return new HashMap<>();
        for(String element : elements)
            if(json.has(element)) {
                Map<String, List<ResourceLocation>> parsedMap = parseResourceMap(json,distinctValues,resourcePath,elements);
                return parsedMap.isEmpty() ? new HashMap<>() : parsedMap;
            }
        return new HashMap<>();
    }

    //once at least 1 element exists, get the map
    private static Map<String, List<ResourceLocation>> parseResourceMap(@Nonnull JsonObject json, boolean distinctValues,
                                                                        String resourcePath, String ... elements) {
        Map<String, List<ResourceLocation>> ret = new HashMap<>();
        for(String element : elements) {
            List<ResourceLocation> potentialResources = potentialResourceList(json,element,resourcePath,distinctValues);
            if(!potentialResources.isEmpty())
                ret.put(element,potentialResources);
        }
        return ret;
    }

    //returns an empty list if the json object is null or something fails to parse into a resource
    public static List<ResourceLocation> potentialResourceList(JsonObject json, String resourcePath,
                                                                         String element, boolean distinct) {
        if(Objects.nonNull(json) && json.has(element)) {
            try {
                List<ResourceLocation> resources = StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                        json.get(element).getAsJsonArray().iterator(),0),false)
                        .filter(JsonElement::isJsonObject).map(JsonObject.class::cast)
                        .map(JsonObject::getAsString).filter(resource -> !resource.isBlank())
                        .map(resource -> ModDefinitions.getResource(resourcePath.replaceFirst("\\{}",resource))).toList();
                if(distinct)
                    resources = resources.stream().distinct().collect(Collectors.toList());
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
