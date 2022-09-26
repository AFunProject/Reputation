package mods.thecomputerizer.reputation;

import mods.thecomputerizer.reputation.api.capability.IPlacedContainer;
import mods.thecomputerizer.reputation.api.capability.IPlayerFaction;
import mods.thecomputerizer.reputation.api.capability.IReputation;
import mods.thecomputerizer.reputation.client.ClientTrackers;
import mods.thecomputerizer.reputation.client.event.RenderEvents;
import mods.thecomputerizer.reputation.client.event.WorldEvents;
import mods.thecomputerizer.reputation.client.render.RenderIcon;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import mods.thecomputerizer.reputation.common.command.ReputationFactionArgument;
import mods.thecomputerizer.reputation.common.network.PacketHandler;
import mods.thecomputerizer.reputation.common.registration.RegistryHandler;
import mods.thecomputerizer.reputation.config.ClientConfigHandler;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = ModDefinitions.MODID)
@Mod.EventBusSubscriber(modid = ModDefinitions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Reputation {

	private static final Logger logger = LogManager.getLogger(ModDefinitions.NAME);

	public Reputation() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerCapabilities);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfigHandler.CONFIG, "reputation/client.toml");
		MinecraftForge.EVENT_BUS.register(this);
		RegistryHandler.registerCommonObjects(FMLJavaModLoadingContext.get().getModEventBus());
	}

	public void commonSetup(FMLCommonSetupEvent event) {
		PacketHandler.initPackets();
		ArgumentTypes.register("reputation:faction_argument", ReputationFactionArgument.class, new EmptyArgumentSerializer<>(ReputationFactionArgument::id));
	}

	public void clientSetup(FMLClientSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(RenderIcon.class);
		MinecraftForge.EVENT_BUS.register(ClientTrackers.class);
		MinecraftForge.EVENT_BUS.register(RenderEvents.class);
		MinecraftForge.EVENT_BUS.register(WorldEvents.class);
	}

	public void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.register(IReputation.class);
		event.register(IPlacedContainer.class);
		event.register(IPlayerFaction.class);
	}

	public static void logInfo(String message, Object... vars) {
		logger.info(message, vars);
	}

	public static void logError(String message, Object... vars) {
		logger.error(message,vars);
	}
}
