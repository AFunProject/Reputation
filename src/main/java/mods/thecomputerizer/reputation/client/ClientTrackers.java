package mods.thecomputerizer.reputation.client;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.reputation.common.ai.ChatTracker;
import mods.thecomputerizer.reputation.network.ReputationNetwork;
import mods.thecomputerizer.reputation.util.HelperMethods;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static mods.thecomputerizer.reputation.ReputationRef.MODID;
import static net.minecraftforge.api.distmarker.Dist.CLIENT;
import static net.minecraftforge.event.TickEvent.Phase.END;

@EventBusSubscriber(modid=MODID,value=CLIENT)
public class ClientTrackers {
    
    private static final Map<EntityType<?>,Data> CLIENT_ICON_DATA = new HashMap<>();

    @SubscribeEvent
    public static void tickTrackers(ClientTickEvent e) {
        if(e.phase==END)
            for(Data clientData : CLIENT_ICON_DATA.values()) clientData.tick();
    }

    public static void onSync(ByteBuf buf) {
        CLIENT_ICON_DATA.clear();
        CLIENT_ICON_DATA.putAll(NetworkHelper.readList(buf,() -> new Data(buf)).stream().filter(Data::isValid)
                .collect(Collectors.toMap(Data::getType,data -> data)));
    }

    public static void setIcons(List<ChatTracker> trackers) {
        LocalPlayer player = Minecraft.getInstance().player;
        if(Objects.nonNull(player)) {
            for(ChatTracker tracker : trackers) {
                Entity entity = player.level.getEntity(tracker.getEntityID());
                if(entity instanceof LivingEntity living && CLIENT_ICON_DATA.containsKey(entity.getType()))
                    CLIENT_ICON_DATA.get(entity.getType()).set(living,tracker.getEvent());
            }
        }
    }

    public static boolean hasIcon(EntityType<?> type) {
        return CLIENT_ICON_DATA.containsKey(type) && CLIENT_ICON_DATA.get(type).isTicking();
    }

    public static ResourceLocation getChatIcon(LivingEntity entity) {
        return hasIcon(entity.getType()) ? CLIENT_ICON_DATA.get(entity.getType()).currentIcons.get(entity) : null;
    }

    public static class Data {
        
        private final EntityType<?> type;
        private final Map<String,List<ResourceLocation>> iconMap;
        private final long displayTimer;
        private final Map<LivingEntity,ResourceLocation> currentIcons;
        private final Map<LivingEntity,MutableInt> currentTimers;
        
        private Data(ByteBuf buf) {
            this.type = ReputationNetwork.readEntityType(buf);
            this.iconMap = NetworkHelper.readMap(buf,() -> NetworkHelper.readString(buf),
                    () -> NetworkHelper.readList(buf,() -> ReputationNetwork.readResourceLocation(buf)));
            this.displayTimer = buf.readLong();
            this.currentIcons = new HashMap<>();
            this.currentTimers = new HashMap<>();
        }

        private void set(LivingEntity entity, String event) {
            if(Objects.nonNull(this.type) && this.iconMap.containsKey(event) && !this.iconMap.get(event).isEmpty()
                    && this.displayTimer>0) {
                ResourceLocation res = HelperMethods.randomListElement(this.iconMap.get(event));
                if(Objects.nonNull(res)) {
                    this.currentIcons.put(entity,res);
                    this.currentTimers.put(entity,new MutableInt(this.displayTimer));
                }
            }
        }

        private boolean isTicking() {
            return !this.currentIcons.isEmpty();
        }

        private void tick() {
            if(!this.currentTimers.isEmpty()) {
                for(MutableInt timer : this.currentTimers.values()) timer.decrement();
                Iterator<Entry<LivingEntity,MutableInt>> itr = this.currentTimers.entrySet().iterator();
                while (itr.hasNext()) {
                    Entry<LivingEntity,MutableInt> entry = itr.next();
                    if(entry.getValue().getValue()<=0) {
                        this.currentIcons.remove(entry.getKey());
                        itr.remove();
                    }
                }
            }
        }

        public boolean isValid() {
            return Objects.nonNull(this.type);
        }

        public EntityType<?> getType() {
            return this.type;
        }
    }
}