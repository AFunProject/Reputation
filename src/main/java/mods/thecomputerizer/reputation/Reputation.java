package mods.thecomputerizer.reputation;

import com.google.gson.JsonElement;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.api.capability.IPlacedContainer;
import mods.thecomputerizer.reputation.api.capability.IPlayerFaction;
import mods.thecomputerizer.reputation.api.capability.IReputation;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import mods.thecomputerizer.reputation.common.network.PacketHandler;
import mods.thecomputerizer.reputation.common.network.SyncChatIconsMessage;
import mods.thecomputerizer.reputation.common.registration.Items;
import mods.thecomputerizer.reputation.common.registration.RegistryHandler;
import mods.thecomputerizer.reputation.config.ClientConfigHandler;
import mods.thecomputerizer.reputation.util.HelperMethods;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mod(value = ModDefinitions.MODID)
@Mod.EventBusSubscriber(modid = ModDefinitions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Reputation {

	private static final Logger logger = LogManager.getLogger(ModDefinitions.NAME);
	public static final List<JsonElement> chatIconData = new ArrayList<>();

	public Reputation() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerReload);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerCapabilities);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfigHandler.CONFIG, "reputation/client.toml");
		MinecraftForge.EVENT_BUS.register(this);
		RegistryHandler.registerCommonObjects(FMLJavaModLoadingContext.get().getModEventBus());
	}

	public void commonSetup(FMLCommonSetupEvent event) {
		PacketHandler.initPackets();
	}

	public void clientSetup(FMLClientSetupEvent event) {
		ItemProperties.register(Items.FACTION_BAG.get(), new ResourceLocation(ModDefinitions.MODID, "faction_bag_type"),
				(stack, level, entity, seed) -> {
					float ret = HelperMethods.getOneAboveIntegerIndexOfMapForValue(ReputationHandler.FACTION_CURRENCY_MAP,
							Collections.singletonList(ReputationHandler.FACTION_CURRENCY_MAP.get(
									ForgeRegistries.ITEMS.getValue(
											new ResourceLocation(
													stack.getTag().getCompound("Item")
															.get("id").getAsString())))));
					if (stack.getTag().contains("Signed")) ret += 0.5f;
					return ret;
				});
	}

	public void registerReload(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(new SimplePreparableReloadListener<Void>() {
			@Override
			protected @NotNull Void prepare(@NotNull ResourceManager pResourceManager, @NotNull ProfilerFiller pProfiler) {
				return null;
			}

			@Override
			protected void apply(@NotNull Void pObject, @NotNull ResourceManager pResourceManager, @NotNull ProfilerFiller pProfiler) {
				logInfo("So when is this getting called");
			}
		});
	}

	public void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.register(IReputation.class);
		event.register(IPlacedContainer.class);
		event.register(IPlayerFaction.class);
	}

	public static void syncChatIcons(ServerPlayer player) {
		if(!chatIconData.isEmpty()) PacketHandler.sendTo(new SyncChatIconsMessage(chatIconData.stream().distinct().collect(Collectors.toList())),player);
	}

	public static void logInfo(Object message) {
		logger.info(message);
	}

	public static void logError(Object message, @Nullable Exception e) {
		logger.error(message);
		if(e!=null) e.printStackTrace();
	}
}
