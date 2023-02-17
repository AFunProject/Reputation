package mods.thecomputerizer.reputation.common.ai;

import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.util.NetworkUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.nio.charset.StandardCharsets;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class ChatTracker {

    private long queryTimer;
    private int entityID;
    private boolean recentChat;
    private int chatTimer = 0;
    private boolean changed;
    private boolean random;
    private boolean inRange;
    private boolean engage;
    private boolean flee;
    private String event;
    private EntityType<?> entityType;

    private ChatTracker() {
        this.changed = false;
    }

    public ChatTracker(LivingEntity entity) {
        this.queryTimer = ServerTrackers.getQuery(entity.getType());
        this.entityID = entity.getId();
        this.entityType = entity.getType();
        this.recentChat = false;
        this.changed = false;
        this.random = false;
        this.inRange = false;
        this.engage = false;
        this.flee = false;
        Reputation.logInfo("successfully attached tracker to {}",entityType.getRegistryName());
    }

    private void setID(int id) {
        this.entityID = id;
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

    public void setFlee(boolean flee) {
        this.flee = flee;
    }


    public int getEntityID() {
        return this.entityID;
    }

    public EntityType<?> getEntityType() {
        return this.entityType;
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

    public boolean getFlee() {
        return this.flee;
    }

    public void queryChatTimer() {
        if(this.recentChat) {
            this.chatTimer++;
            if (this.chatTimer >= this.queryTimer) {
                this.chatTimer = 0;
                this.recentChat = false;
            }
        }
    }

    public String getPriorityChatEvent() {
        if(this.flee) return "flee";
        else if(this.engage) return "engage";
        else if(this.inRange) return "idle_faction";
        else if(this.random) return "idle";
        else return "none";
    }

    public String getEvent() {
        return this.event;
    }

    public void encode(FriendlyByteBuf buf) {
        Reputation.logInfo("sending tracker for {}",entityType.getRegistryName());
        buf.writeInt(this.entityID);
        buf.writeInt(this.getPriorityChatEvent().length());
        buf.writeCharSequence(this.getPriorityChatEvent(), StandardCharsets.UTF_8);
        NetworkUtil.writeEntityType(buf,this.entityType);
    }

    public static ChatTracker decode(FriendlyByteBuf buf) {
        ChatTracker ret = new ChatTracker();
        ret.setID(buf.readInt());
        ret.event = (String)buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8);
        ret.entityType = NetworkUtil.readEntityType(buf).orElse(null);
        return ret;
    }
}
