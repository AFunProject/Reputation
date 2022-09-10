package mods.thecomputerizer.reputation.api;

import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.api.capability.IReputation;
import mods.thecomputerizer.reputation.client.event.RenderEvents;
import mods.thecomputerizer.reputation.common.network.PacketHandler;
import mods.thecomputerizer.reputation.common.network.SetIconMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;

import java.util.*;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class ReputationHandler {

	public static Capability<IReputation> REPUTATION_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
	private static HashMap<ResourceLocation, Faction> FACTIONS = new HashMap<>();
	public static HashMap<Item, Faction> FACTION_CURRENCY_MAP = new HashMap<>();
	public static Random random = new Random();

	public static void registerFaction(Faction faction) {
		if(!faction.getID().toString().isEmpty() && !FACTIONS.containsKey(faction.getID())) {
			Reputation.logInfo("registered faction at location " + faction.getID().toString());
			FACTIONS.put(faction.getID(), faction);
			FACTION_CURRENCY_MAP.put(faction.getCurrencyItem(), faction);
		}
	}

	public static Faction getFaction(ResourceLocation loc) {
		return getFactionMap().get(loc);
	}

	public static Map<ResourceLocation, Faction> getFactionMap() {
		return FACTIONS;
	}

	public static Collection<Faction> getEntityFactions(LivingEntity entity) {
		Set<Faction> factions = new HashSet<>();
		for (Faction faction : getFactionMap().values()) if (faction.isMember(entity)) factions.add(faction);
		return factions;
	}

	public static int getReputation(Player player, Faction faction) {
		LazyOptional<IReputation> optional = player.getCapability(ReputationHandler.REPUTATION_CAPABILITY);
		if (optional.isPresent()) {
			IReputation reputation = optional.resolve().get();
			return reputation.getReputation(faction);
		}
		return 0;
	}

	public static void changeReputation(Player player, Faction faction, int amount) {
		if(amount!=0 && player instanceof ServerPlayer) {
			LazyOptional<IReputation> optional = player.getCapability(ReputationHandler.REPUTATION_CAPABILITY);
			if (optional.isPresent()) {
				IReputation reputation = optional.resolve().get();
				reputation.changeReputation(player, faction, amount);
				PacketHandler.sendTo(new SetIconMessage(amount>0,faction.getID(),amount),(ServerPlayer)player);
			}
		}
	}
	public static boolean isGoodReputation(Player player, Faction faction) {
		LazyOptional<IReputation> optional = player.getCapability(ReputationHandler.REPUTATION_CAPABILITY);
		if (optional.isPresent()) {
			IReputation reputation = optional.resolve().get();
			return reputation.getReputation(faction)>=faction.getHigherRep();
		}
		return false;
	}

	public static boolean isBadReputation(Player player, Faction faction) {
		LazyOptional<IReputation> optional = player.getCapability(ReputationHandler.REPUTATION_CAPABILITY);
		if (optional.isPresent()) {
			IReputation reputation = optional.resolve().get();
			return reputation.getReputation(faction)<=faction.getLowerRep();
		}
		return false;
	}

	public static void emptyMaps() {
		FACTIONS = new HashMap<>();
	}

	public static void readPacketData(HashMap<Faction, Integer> factions) {
		for (Faction faction : factions.keySet()) {
			RenderEvents.CLIENT_FACTIONS.put(faction.getID(),faction);
			RenderEvents.CLIENT_FACTIONS_REPUTATION.put(faction, factions.get(faction));
		}
	}
}
