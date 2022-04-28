package mods.thecomputerizer.reputation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.api.capability.IPlacedContainer;
import mods.thecomputerizer.reputation.api.capability.IPlayerFaction;
import mods.thecomputerizer.reputation.api.capability.IReputation;
import mods.thecomputerizer.reputation.client.RenderIcon;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import mods.thecomputerizer.reputation.common.ai.ReputationMemoryModule;
import mods.thecomputerizer.reputation.common.ai.ReputationSenorType;
import mods.thecomputerizer.reputation.common.network.PacketHandler;
import mods.thecomputerizer.reputation.common.network.SyncFactionsMessage;
import mods.thecomputerizer.reputation.common.registration.TagKeys;
import mods.thecomputerizer.reputation.config.ClientConfigHandler;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Mod(value = ModDefinitions.MODID)
@Mod.EventBusSubscriber(modid = ModDefinitions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Reputation {

	private static final Logger logger = LogManager.getLogger(ModDefinitions.NAME);

	private static final Gson GSON = Util.make(() -> {
		GsonBuilder builder = new GsonBuilder();
		builder.disableHtmlEscaping();
		builder.setLenient();
		builder.setPrettyPrinting();
		return builder.create();
	});

	public Reputation() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonsetup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerCapabilities);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfigHandler.CONFIG, "reputation/client.toml");
		MinecraftForge.EVENT_BUS.register(this);
		if(FMLEnvironment.dist == Dist.CLIENT) MinecraftForge.EVENT_BUS.register(RenderIcon.class);
		MinecraftForge.EVENT_BUS.addListener(this::reloadData);
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		ReputationMemoryModule.MEMORY_MODULES.register(modBus);
		ReputationSenorType.SENSOR_TYPES.register(modBus);
		TagKeys.init(modBus);
	}

	public void commonsetup(FMLCommonSetupEvent event) {
		PacketHandler.initPackets();
	}

	public void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.register(IReputation.class);
		event.register(IPlacedContainer.class);
		event.register(IPlayerFaction.class);
	}

	@SubscribeEvent
	public void reloadData(AddReloadListenerEvent event) {
		event.addListener((new SimplePreparableReloadListener<Void>() {
			@Nonnull
			@Override
			protected Void prepare(@Nonnull ResourceManager rm, @Nonnull ProfilerFiller profiler) {
				return null;
			}

			@Override
			protected void apply(@Nonnull Void value, @Nonnull ResourceManager rm, @Nonnull ProfilerFiller profiler) {
				try {
					ReputationHandler.emptyMaps();
					for (ResourceLocation resource : rm.listResources("factions/", (location) -> location.endsWith("json"))) {
						boolean good = true;
						for(Faction f : ReputationHandler.getServerFactions()) {
							if(f.getName().toString().matches(resource.toString())) {
								good = false;
								break;
							}
						}
						if(good) {
							InputStreamReader reader = new InputStreamReader(rm.getResource(resource).getInputStream(), StandardCharsets.UTF_8);
							ReputationHandler.registerFaction(Faction.fromJson(resource, GSON.fromJson(reader, JsonElement.class)));
							reader.close();
							Reputation.logInfo("registered faction at location " + resource);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("Failed to read faction data!");
				}
			}
		}));
	}

	public static void logInfo(Object message) {
		logger.info(message);
	}

	public static void logError(Object message, Exception e) {
		logger.error(message);
		e.printStackTrace();
	}
}
