package mods.thecomputerizer.reputation.client;

import mods.thecomputerizer.reputation.common.ModDefinitions;
import mods.thecomputerizer.reputation.common.ai.ChatTracker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = ModDefinitions.MODID)
public class ClientTrackers {
    private static final Random random = new Random();
    public static final HashMap<ChatTracker, Integer> trackerMap = new HashMap<>();
    public static HashMap<ChatTracker, ResourceLocation> selectedIconMap = new HashMap<>();
    public static HashMap<EntityType<?>, HashMap<String, List<ResourceLocation>>> iconMap = new HashMap<>();

    @SubscribeEvent
    public static void tickTrackers(TickEvent.ClientTickEvent e) {
        if(e.phase==TickEvent.Phase.END) {
            List<ChatTracker> toRemove = new ArrayList<>();
            for (ChatTracker tracker : trackerMap.keySet()) {
                trackerMap.put(tracker, trackerMap.get(tracker) + 1);
                if (trackerMap.get(tracker) >= 50) toRemove.add(tracker);
            }
            if (!toRemove.isEmpty()) for (ChatTracker tracker : toRemove) {
                trackerMap.remove(tracker);
                selectedIconMap.remove(tracker);
            }
        }
    }

    public static void initTracker(ChatTracker tracker) {
        if(iconMap.get(tracker.getEntityType())!=null) {
            List<ResourceLocation> icons = iconMap.get(tracker.getEntityType()).get(tracker.getEvent());
            if (!icons.isEmpty()) {
                trackerMap.put(tracker, 0);
                selectedIconMap.put(tracker, icons.get(random.nextInt(icons.size())));
            }
        }
    }

    public static ResourceLocation getChatIcon(LivingEntity entity) {
        ChatTracker tracker = getEntityTrackerOrNull(entity);
        if(tracker==null) return null;
        return selectedIconMap.get(tracker);
    }

    public static ChatTracker getEntityTrackerOrNull(LivingEntity entity) {
        for(ChatTracker tracker : trackerMap.keySet()) {
            if(tracker.getEntityUUID().toString().matches(entity.getUUID().toString())) return tracker;
        }
        return null;
    }


}
