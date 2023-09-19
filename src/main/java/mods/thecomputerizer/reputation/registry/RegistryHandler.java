package mods.thecomputerizer.reputation.registry;

import mods.thecomputerizer.reputation.Constants;
import mods.thecomputerizer.reputation.common.ai.ReputationMemoryModule;
import mods.thecomputerizer.reputation.common.ai.ReputationSenorType;
import mods.thecomputerizer.reputation.capability.FactionListener;
import mods.thecomputerizer.reputation.network.*;
import mods.thecomputerizer.theimpossiblelibrary.network.NetworkHandler;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public class RegistryHandler {

    public static final CreativeModeTab REPUTATION_TAB = new CreativeModeTab(Constants.MODID) {
        @OnlyIn(Dist.CLIENT)
        public @Nonnull ItemStack makeIcon() {
            return new ItemStack(ItemRegistry.LEDGER_BOOK_ITEM.get());
        }
    };

    public static void initRegistries(IEventBus bus) {
        BlockRegistry.register(bus);
        ItemRegistry.register(bus);
        RecipeRegistry.register(bus);
        BlockEntitiesRegistry.register(bus);
        SoundRegistry.register(bus);
        ReputationMemoryModule.MEMORY_MODULES.register(bus);
        ReputationSenorType.SENSOR_TYPES.register(bus);
    }

    public static void queuePackets() {
        NetworkHandler.queuePacketRegisterToClient(PacketChatIcon.class,PacketChatIcon::new);
        NetworkHandler.queuePacketRegisterToClient(PacketFleeIcon.class,PacketFleeIcon::new);
        NetworkHandler.queuePacketRegisterToClient(PacketSetIcon.class,PacketSetIcon::new);
        NetworkHandler.queuePacketRegisterToClient(PacketSyncChatIcons.class,PacketSyncChatIcons::new);
        NetworkHandler.queuePacketRegisterToClient(PacketSyncFactionPlayers.class,PacketSyncFactionPlayers::new);
        NetworkHandler.queuePacketRegisterToClient(PacketSyncFactions.class,PacketSyncFactions::new);
        NetworkHandler.queuePacketRegisterToClient(PacketSyncReputation.class,PacketSyncReputation::new);
    }

    @SubscribeEvent
    public void reloadData(AddReloadListenerEvent event) {
        event.addListener(new FactionListener());
    }
}
