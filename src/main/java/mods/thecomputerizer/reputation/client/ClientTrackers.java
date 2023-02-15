package mods.thecomputerizer.reputation.client;

import mods.thecomputerizer.reputation.common.ai.ChatTracker;
import mods.thecomputerizer.reputation.util.NetworkUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;
import java.util.stream.Collectors;

public class ClientTrackers {
    private static final HashMap<EntityType<?>, Data> CLIENT_ICON_DATA = new HashMap<>();

    @SubscribeEvent
    public static void tickTrackers(TickEvent.ClientTickEvent e) {
        if(e.phase==TickEvent.Phase.END)
            for(Data clientData : CLIENT_ICON_DATA.values())
                clientData.tick();
    }

    public static void onSync(FriendlyByteBuf buf) {
        CLIENT_ICON_DATA.clear();
        CLIENT_ICON_DATA.putAll(NetworkUtil.readGenericList(buf,Data::new).stream().filter(Data::isValid)
                .collect(Collectors.toMap(Data::getType,data -> data)));
    }

    public static void setIcons(List<ChatTracker> trackers) {
        if(Objects.nonNull(Minecraft.getInstance().player)) {
            for(ChatTracker tracker : trackers) {
                Entity entity = Minecraft.getInstance().player.level.getEntity(tracker.getEntityID());
                if (entity instanceof LivingEntity living && CLIENT_ICON_DATA.containsKey(entity.getType()))
                    CLIENT_ICON_DATA.get(entity.getType()).set(living, tracker.getEvent());
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
        private final Random selector;
        private final EntityType<?> type;
        private final Map<String, List<ResourceLocation>> iconMap;
        private final long displayTimer;
        private final Map<LivingEntity, ResourceLocation> currentIcons;
        private final Map<LivingEntity, MutableInt> currentTimers;
        private Data(FriendlyByteBuf buf) {
            this.selector = new Random();
            this.type = NetworkUtil.readEntityType(buf).orElse(null);
            this.iconMap = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,
                    buf1 -> NetworkUtil.readGenericList(buf1,FriendlyByteBuf::readResourceLocation));
            this.displayTimer = buf.readLong();
            this.currentIcons = new HashMap<>();
            this.currentTimers = new HashMap<>();
        }

        private void set(LivingEntity entity, String event) {
            if(Objects.nonNull(this.type) && this.iconMap.containsKey(event) && !this.iconMap.get(event).isEmpty()
                    && this.displayTimer>0) {
                this.currentIcons.put(entity,this.iconMap.get(event).get(this.selector.nextInt(this.iconMap.get(event).size())));
                this.currentTimers.put(entity,new MutableInt(this.displayTimer));
            }
        }

        private boolean isTicking() {
            return !this.currentIcons.isEmpty();
        }

        private void tick() {
            if(!this.currentTimers.isEmpty()) {
                for (MutableInt timer : this.currentTimers.values())
                    timer.decrement();
                Iterator<Map.Entry<LivingEntity, MutableInt>> itr = this.currentTimers.entrySet().iterator();
                while (itr.hasNext()) {
                    Map.Entry<LivingEntity, MutableInt> entry = itr.next();
                    if (entry.getValue().getValue() <= 0) {
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