package mods.thecomputerizer.reputation.common.ai;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import mods.thecomputerizer.reputation.common.network.PacketHandler;
import mods.thecomputerizer.reputation.common.network.SyncChatIconsMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ServerTrackers {
    public static final List<JsonElement> chatIconJsonData = new ArrayList<>();
    public static final HashMap<EntityType<?>, HashMap<String, List<ResourceLocation>>> serverIconMap = new HashMap<>();
    public static boolean iconsLoaded = false;

    public static void syncChatIcons(ServerPlayer player) {
        if(!chatIconJsonData.isEmpty()) {
            PacketHandler.sendTo(new SyncChatIconsMessage(chatIconJsonData.stream().distinct().collect(Collectors.toList())),player);
            for(JsonElement elements : chatIconJsonData) {
                JsonObject json = elements.getAsJsonObject();
                EntityType<?> type = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(json.get("name").getAsString()));
                serverIconMap.put(type,new HashMap<>());
                serverIconMap.get(type).put("idle",parseResourceArray("idle",json));
                serverIconMap.get(type).put("idle_faction",parseResourceArray("idle_faction",json));
                serverIconMap.get(type).put("engage",parseResourceArray("engage",json));
            }
            iconsLoaded = true;
        }
    }

    private static List<ResourceLocation> parseResourceArray(String element, JsonObject json) {
        List<ResourceLocation> members = new ArrayList<>();
        if(json.has(element)) {
            for (JsonElement index : json.get(element).getAsJsonArray()) {
                ResourceLocation icon = new ResourceLocation(ModDefinitions.MODID, "textures/chat/"+index.getAsString()+".png");
                if(!members.contains(icon)) members.add(icon);
            }
        }
        return members;
    }

    public static boolean hasIconsForEvent(EntityType<?> type, String event) {
        if(serverIconMap.isEmpty()) return false;
        return serverIconMap.containsKey(type) && !serverIconMap.get(type).isEmpty() && !serverIconMap.get(type).get(event).isEmpty();
    }
}
