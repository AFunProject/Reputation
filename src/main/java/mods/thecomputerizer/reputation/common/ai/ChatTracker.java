package mods.thecomputerizer.reputation.common.ai;

import mods.thecomputerizer.reputation.Reputation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;

public class ChatTracker {

    private UUID entityUUID;
    private long seed;
    private boolean recentChat;
    private int chatTimer = 0;
    private boolean changed;
    private boolean random;
    private boolean inRange;
    private boolean engage;
    private String event;
    private ResourceLocation entityType;

    private ChatTracker() {
        this.changed = false;
    }

    public ChatTracker(LivingEntity entity) {
        this.entityUUID = entity.getUUID();
        this.entityType = entity.getType().getRegistryName();
        this.seed = new Random().nextLong(Long.MAX_VALUE);
        this.recentChat = false;
        this.changed = false;
        this.random = false;
        this.inRange = false;
        this.engage = false;
    }

    private void setUUID(UUID uuid) {
        this.entityUUID = uuid;
    }

    private void setSeed(long seed) {
        this.seed = seed;
    }

    public void setRecent(boolean recent) {
        this.recentChat = recent;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public void setRandom(boolean random) {
        this.random = random;
    }

    public void setInRange(boolean inRange) {
        this.inRange = inRange;
    }

    public void setEngage(boolean engage) {
        this.engage = engage;
    }

    public UUID getEntityUUID() {
        return this.entityUUID;
    }

    public EntityType<?> getEntityType() {
        return ForgeRegistries.ENTITIES.getValue(this.entityType);
    }

    public long getSeed() {
        return this.seed;
    }

    public boolean getRecent() {
        return this.recentChat;
    }

    public boolean getChanged() {
        return this.changed;
    }

    public boolean getRandom() {
        return this.random;
    }

    public boolean getInRange() {
        return this.inRange;
    }

    public boolean getEngage() {
        return this.engage;
    }

    public void queryChatTimer() {
        if(this.recentChat) {
            this.chatTimer++;
            if (chatTimer >= 200) {
                chatTimer = 0;
                this.recentChat = false;
            }
        }
    }

    public String getPriorityChatEvent() {
        if(this.engage) return "engage";
        else if(this.random) {
            if(this.inRange) return "idle_faction";
            else return "idle";
        }
        else return "none";
    }

    public String getEvent() {
        return this.event;
    }

    public void encode(FriendlyByteBuf buf) {
        Reputation.logInfo("write UUID "+this.getEntityUUID());
        buf.writeUUID(this.entityUUID);
        buf.writeLong(this.seed);
        buf.writeInt(this.getPriorityChatEvent().length());
        buf.writeCharSequence(this.getPriorityChatEvent(), StandardCharsets.UTF_8);
        buf.writeResourceLocation(this.entityType);
    }

    public static ChatTracker decode(FriendlyByteBuf buf) {
        ChatTracker ret = new ChatTracker();
        ret.setUUID(buf.readUUID());
        Reputation.logInfo("read UUID "+ret.getEntityUUID());
        ret.setSeed(buf.readLong());
        ret.event = (String)buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8);
        Reputation.logInfo("read in event as "+ret.event);
        ret.entityType = buf.readResourceLocation();
        return ret;
    }
}
