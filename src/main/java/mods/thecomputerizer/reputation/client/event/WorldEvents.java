package mods.thecomputerizer.reputation.client.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;

@OnlyIn(value = Dist.CLIENT)
public class WorldEvents {

    @SubscribeEvent
    public static void onDisconnect(ClientPlayerNetworkEvent.LoggedOutEvent e) {
        RenderEvents.CLIENT_FACTIONS = new HashMap<>();
    }
}
