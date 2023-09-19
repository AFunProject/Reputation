package mods.thecomputerizer.reputation.common.ai;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mods.thecomputerizer.reputation.Constants;
import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.network.PacketSyncChatIcons;
import mods.thecomputerizer.reputation.util.JsonUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

import java.util.*;
import java.util.stream.Collectors;

public class ServerTrackers {
    public static final HashMap<EntityType<?>, Data> SERVER_ICON_DATA = new HashMap<>();
    private static float DEFAULT_CHANCE;
    private static float DEFAULT_CHANCE_VARIATION;
    public static long DEFAULT_COOLDOWN;
    public static long DEFAULT_DISPLAY;

    public static void initTimers(JsonElement element) {
        JsonObject json = element instanceof JsonObject ? element.getAsJsonObject() : null;
        DEFAULT_CHANCE = JsonUtil.potentialFloat(json,"default_chance").orElse(0.5f);
        DEFAULT_CHANCE_VARIATION = JsonUtil.potentialFloat(json,"default_chance_variation").orElse(0.1f);
        DEFAULT_COOLDOWN = JsonUtil.potentialLong(json,"default_icon_cooldown_time").orElse(100L);
        DEFAULT_DISPLAY = JsonUtil.potentialLong(json,"default_icon_display_time").orElse(50L);
    }

    public static void parseChatIcons(List<JsonElement> jsons) {
        SERVER_ICON_DATA.clear();
        SERVER_ICON_DATA.putAll(jsons.stream().filter(JsonObject.class::isInstance).map(JsonObject.class::cast)
                .map(Data::new).filter(Data::isValid).collect(Collectors.toMap(Data::getType,data -> data)));
    }

    public static void syncChatIcons(ServerPlayer player) {
        if(!SERVER_ICON_DATA.isEmpty())
            new PacketSyncChatIcons(new ArrayList<>(SERVER_ICON_DATA.values())).addPlayers(player).send();
    }

    public static boolean hasAnyIcons() {
        for(EntityType<?> type : SERVER_ICON_DATA.keySet())
            if(hasAnyIcons(type)) return true;
        return false;
    }

    public static boolean hasAnyIcons(EntityType<?> type) {
        return SERVER_ICON_DATA.containsKey(type);
    }

    public static boolean hasIconsForEvent(EntityType<?> type, String event) {
        return SERVER_ICON_DATA.containsKey(type) && SERVER_ICON_DATA.get(type).hasEventForEntity(type,event);
    }

    public static long getQuery(EntityType<?> type) {
        return SERVER_ICON_DATA.get(type).queryTimer;
    }

    public static boolean rollRandom(EntityType<?> type) {
        float min = Math.max(0f,SERVER_ICON_DATA.get(type).chance-SERVER_ICON_DATA.get(type).chanceVariation);
        float max = Math.min(1f,SERVER_ICON_DATA.get(type).chance+SERVER_ICON_DATA.get(type).chanceVariation);
        float rand1 = Constants.floatRand(min,max);
        float rand2 = Constants.floatRand();
        return rand1>rand2;
    }

    public static class Data {
        private final EntityType<?> type;
        private final Map<String, List<ResourceLocation>> iconMap;
        private final long queryTimer;
        private final long displayTimer;
        private final float chance;
        private final float chanceVariation;
        private Data(JsonObject json) {
            this.type = JsonUtil.potentialEntity(json,"name").orElse(null);
            this.iconMap = JsonUtil.potentialResourceMap(json,true,"textures/chat/{}.png",
                    "idle","idle_faction","engage","flee");
            Optional<Float> potentialChance = JsonUtil.potentialFloat(json,"chance");
            this.chance = potentialChance.orElse(DEFAULT_CHANCE);
            potentialChance = JsonUtil.potentialFloat(json,"chance_variation");
            this.chanceVariation = potentialChance.orElse(DEFAULT_CHANCE_VARIATION);
            OptionalLong potentialTime = JsonUtil.potentialLong(json,"query_timer");
            this.queryTimer = potentialTime.orElse(DEFAULT_COOLDOWN);
            potentialTime = JsonUtil.potentialLong(json,"display_timer");
            this.displayTimer = potentialTime.orElse(DEFAULT_DISPLAY);
            Reputation.logInfo("Read in entity {} with {} icon sets",this.type,this.iconMap.size());
        }

        private boolean isValid() {
            return Objects.nonNull(this.type) && !this.iconMap.isEmpty();
        }

        private boolean hasEventForEntity(EntityType<?> type, String event) {
            return Objects.nonNull(type) && this.iconMap.containsKey(event);
        }

        public EntityType<?> getType() {
            return this.type;
        }

        public void encode(FriendlyByteBuf buf) {
            NetworkUtil.writeEntityType(buf,this.type);
            NetworkUtil.writeGenericMap(buf,this.iconMap,NetworkUtil::writeString,(buf1,list) ->
                    NetworkUtil.writeGenericList(buf1,list,FriendlyByteBuf::writeResourceLocation));
            buf.writeLong(displayTimer);
        }
    }
}
