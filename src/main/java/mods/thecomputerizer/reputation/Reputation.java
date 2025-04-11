package mods.thecomputerizer.reputation;

import mods.thecomputerizer.reputation.capability.FactionListener;
import mods.thecomputerizer.reputation.capability.placedcontainer.IPlacedContainer;
import mods.thecomputerizer.reputation.capability.playerfaction.IPlayerFaction;
import mods.thecomputerizer.reputation.capability.reputation.IReputation;
import mods.thecomputerizer.reputation.common.command.ReputationFactionArgument;
import mods.thecomputerizer.reputation.network.ReputationNetwork;
import mods.thecomputerizer.reputation.registry.RegistryHandler;
import mods.thecomputerizer.theimpossiblelibrary.api.core.CoreAPI;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Collection;

import static mods.thecomputerizer.reputation.ReputationRef.LOGGER;
import static mods.thecomputerizer.reputation.ReputationRef.MODID;
import static mods.thecomputerizer.reputation.client.ClientConfigHandler.CONFIG;
import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;
import static net.minecraftforge.fml.config.ModConfig.Type.CLIENT;

@Mod(value=MODID)
public class Reputation {

	public Reputation() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::commonSetup);
		bus.addListener(this::registerCapabilities);
		EVENT_BUS.addListener(this::reloadData);
		RegistryHandler.initRegistries(bus);
		ReputationNetwork.initCommon();
		if(CoreAPI.isClient()) {
			ReputationNetwork.initClient();
			ModLoadingContext.get().registerConfig(CLIENT,CONFIG,"reputation/client.toml");
		}
	}

	public void commonSetup(FMLCommonSetupEvent event) {
		ArgumentTypes.register("reputation:faction_argument",ReputationFactionArgument.class,
				new EmptyArgumentSerializer<>(ReputationFactionArgument::id));
	}

	public void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.register(IReputation.class);
		event.register(IPlacedContainer.class);
		event.register(IPlayerFaction.class);
	}

	@SubscribeEvent
	public void reloadData(AddReloadListenerEvent event) {
		event.addListener(new FactionListener());
	}

	public static void logInfo(String message, Object ... args) {
		LOGGER.info(message,args);
	}

	public static void logError(String message, Object ... args) {
		LOGGER.error(message,args);
	}

	public static void logStringCollection(String initialMessage, Collection<String> collection, int numPerLine) {
		StringBuilder builder = new StringBuilder();
		builder.append(initialMessage).append(": ");
		int i = 0;
		for(String element : collection) {
			builder.append("[").append(i).append("] ").append(element);
			if(i<collection.size()-1) {
				if((i+1)%numPerLine==0) builder.append("\n");
				else builder.append(" ");
				i++;
			}
		}
		LOGGER.info(builder.toString());
	}
}