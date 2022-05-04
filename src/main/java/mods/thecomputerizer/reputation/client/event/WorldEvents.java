package mods.thecomputerizer.reputation.client.event;

import mods.thecomputerizer.reputation.common.ModDefinitions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;

@Mod.EventBusSubscriber(modid = ModDefinitions.MODID)
@OnlyIn(value = Dist.CLIENT)
public class WorldEvents {

    @SubscribeEvent
    public static void onDisconnect(ClientPlayerNetworkEvent.LoggedOutEvent e) {
        RenderEvents.CLIENT_FACTIONS = new HashMap<>();
    }
}
