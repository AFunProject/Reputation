package mods.thecomputerizer.reputation.api;

import com.google.gson.JsonElement;
import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.api.capability.IReputation;
import mods.thecomputerizer.reputation.client.ClientHandler;
import mods.thecomputerizer.reputation.client.event.RenderEvents;
import mods.thecomputerizer.reputation.common.ai.ReputationStandings;
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


public class ReputationHandler {

	public static Capability<IReputation> REPUTATION_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
	private static HashMap<ResourceLocation, Faction> FACTIONS = new HashMap<>();
	public static Random RANDOM = new Random();

	public static void registerFaction(Faction faction) {
		if(!faction.getID().toString().isEmpty() && !FACTIONS.containsKey(faction.getID())) {
			Reputation.logInfo("registered faction at location {}",faction.getID().toString());
			FACTIONS.put(faction.getID(), faction);
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

	public static Faction getFactionFromCurrency(Item currencyItem) {
		for(Faction faction : FACTIONS.values())
			if(faction.getCurrencyItem()==currencyItem)
				return faction;
		return null;
	}

	public static IReputation getCapability(Player player) {
		LazyOptional<IReputation> optional = player.getCapability(ReputationHandler.REPUTATION_CAPABILITY);
		return optional.isPresent() ? optional.resolve().isPresent() ? optional.resolve().get() : null : null;
	}

	public static int getReputation(Player player, Faction faction) {
		IReputation reputation = getCapability(player);
		return Objects.nonNull(reputation) ? reputation.getReputation(faction) : 0;
	}

	public static void changeReputation(Player player, Faction faction, int amount) {
		if(amount!=0 && player instanceof ServerPlayer) {
			IReputation reputation = getCapability(player);
			if (Objects.nonNull(reputation)) {
				reputation.changeReputation(player, faction, amount);
				PacketHandler.sendTo(new SetIconMessage(amount>0,faction.getID(),amount),(ServerPlayer)player);
			}
		}
	}
	public static boolean isGoodReputation(Player player, Faction faction) {
		IReputation reputation = getCapability(player);
		if(Objects.isNull(reputation)) return false;
		return getReputation(player, faction)>=faction.getHigherRep();
	}

	public static boolean isBadReputation(Player player, Faction faction) {
		IReputation reputation = getCapability(player);
		if(Objects.isNull(reputation)) return false;
		return getReputation(player, faction)<=faction.getHigherRep();
	}

	public static void emptyMaps() {
		FACTIONS = new HashMap<>();
	}

	public static void readPacketData(HashMap<Faction, Integer> factions, JsonElement reputationStandingsData) {
		ClientHandler.standings = new ReputationStandings(reputationStandingsData);
		for (Faction faction : factions.keySet()) {
			RenderEvents.CLIENT_FACTIONS.put(faction.getID(),faction);
			RenderEvents.CLIENT_FACTIONS_REPUTATION.put(faction, factions.get(faction));
		}
	}
}
