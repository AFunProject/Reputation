package mods.thecomputerizer.reputation.common.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mods.thecomputerizer.reputation.client.ClientTrackers;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class SyncChatIconsMessage {

    private final List<JsonElement> jsonData = new ArrayList<>();

    public SyncChatIconsMessage(FriendlyByteBuf buf){
        int files = buf.readInt();
        for(int i = 0;i<files;i++) {
            ResourceLocation entity = buf.readResourceLocation();
            EntityType<?> type = getEntityFromResource(entity);
            ClientTrackers.iconMap.put(type,new HashMap<>());
            int j,icons;
            icons = buf.readInt();
            List<ResourceLocation> random = new ArrayList<>();
            for(j = 0;j<icons;j++) random.add(buf.readResourceLocation());
            ClientTrackers.iconMap.get(type).put("idle",random);
            List<ResourceLocation> random_faction = new ArrayList<>();
            icons = buf.readInt();
            for(j = 0;j<icons;j++) random_faction.add(buf.readResourceLocation());
            ClientTrackers.iconMap.get(type).put("idle_faction",random_faction);
            List<ResourceLocation> engage = new ArrayList<>();
            icons = buf.readInt();
            for(j = 0;j<icons;j++) engage.add(buf.readResourceLocation());
            ClientTrackers.iconMap.get(type).put("engage",engage);
        }
    }

    public SyncChatIconsMessage(List<JsonElement> jsonData) {
        this.jsonData.addAll(jsonData);
    }

    public static void encode(SyncChatIconsMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.jsonData.size());
        for(JsonElement json : message.jsonData) parseEvents(json,buf);
    }

    public static void handle(SyncChatIconsMessage ignoredMessage, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() ->  {});
        ctx.setPacketHandled(true);
    }

    private static void parseEvents(JsonElement data, FriendlyByteBuf buf) {
        try {
            JsonObject json = data.getAsJsonObject();
            buf.writeResourceLocation(new ResourceLocation(json.get("name").getAsString()));
            List<ResourceLocation> random = parseResourceArray("idle",json);
            buf.writeInt(random.size());
            for(ResourceLocation rl : random) buf.writeResourceLocation(rl);
            List<ResourceLocation> random_faction = parseResourceArray("idle_faction",json);
            buf.writeInt(random_faction.size());
            for(ResourceLocation rl : random_faction) buf.writeResourceLocation(rl);
            List<ResourceLocation> combat = parseResourceArray("engage",json);
            buf.writeInt(combat.size());
            for(ResourceLocation rl : combat) buf.writeResourceLocation(rl);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to parse chat icon data!");
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

    private static EntityType<?> getEntityFromResource(ResourceLocation rl) {
        return ForgeRegistries.ENTITIES.getValue(rl);
    }
}
