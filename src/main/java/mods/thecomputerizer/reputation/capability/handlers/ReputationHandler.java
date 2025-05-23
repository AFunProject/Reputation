package mods.thecomputerizer.reputation.capability.handlers;

import com.google.gson.JsonElement;
import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.capability.Faction;
import mods.thecomputerizer.reputation.capability.reputation.IReputation;
import mods.thecomputerizer.reputation.client.ClientHandler;
import mods.thecomputerizer.reputation.common.ai.ReputationStandings;
import mods.thecomputerizer.reputation.network.PacketSetIcon;
import mods.thecomputerizer.reputation.network.ReputationNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.*;

import static mods.thecomputerizer.reputation.capability.reputation.ReputationProvider.REPUTATION_CAPABILITY;
import static mods.thecomputerizer.reputation.client.ClientEvents.CLIENT_FACTIONS;
import static mods.thecomputerizer.reputation.client.ClientEvents.CLIENT_FACTIONS_REPUTATION;

public class ReputationHandler {
	
	private static final Map<ResourceLocation,Faction> FACTIONS = new HashMap<>();

	public static void registerFaction(Faction faction) {
		if(!faction.getID().toString().isEmpty() && !FACTIONS.containsKey(faction.getID())) {
			Reputation.logInfo("registered faction at location {}",faction.getID().toString());
			FACTIONS.put(faction.getID(),faction);
		}
	}
	
	@SuppressWarnings("removal")
	public static Faction getFaction(String location) {
		return getFaction(new ResourceLocation(location));
	}

	public static Faction getFaction(ResourceLocation res) {
		return FACTIONS.get(res);
	}

	public static Map<ResourceLocation,Faction> getFactionMap() {
		return FACTIONS;
	}

	public static Collection<Faction> getEntityFactions(LivingEntity entity) {
		Set<Faction> factions = new HashSet<>();
		for(Faction faction : FACTIONS.values())
			if(faction.isMember(entity)) factions.add(faction);
		return factions;
	}

	public static Faction getFactionFromCurrency(Item currencyItem) {
		for(Faction faction : FACTIONS.values())
			if(faction.getCurrencyItem()==currencyItem)
				return faction;
		return null;
	}

	@SuppressWarnings("DataFlowIssue")
	public static @Nullable IReputation getCapability(Player player) {
		return player.getCapability(REPUTATION_CAPABILITY).orElse(null);
	}

	public static int getReputation(Player player, Faction faction) {
		IReputation reputation = getCapability(player);
		return Objects.nonNull(reputation) ? reputation.getReputation(faction) : 0;
	}

	public static void changeReputation(Player p, Faction faction, int amount) {
		if(amount!=0 && p instanceof ServerPlayer player) {
			IReputation reputation = getCapability(player);
			if(Objects.nonNull(reputation)) {
				reputation.changeReputation(player, faction, amount);
				ReputationNetwork.sendToClient(new PacketSetIcon(amount>0,faction.getID(),amount),player);
			}
		}
	}
	public static boolean isGoodReputation(Player player, Faction faction) {
		IReputation reputation = getCapability(player);
		if(Objects.isNull(reputation)) return false;
		return getReputation(player,faction)>=faction.getHigherRep();
	}

	public static boolean isBadReputation(Player player, Faction faction) {
		IReputation reputation = getCapability(player);
		if(Objects.isNull(reputation)) return false;
		return getReputation(player, faction)<=faction.getHigherRep();
	}

	public static void emptyMaps() {
		FACTIONS.clear();
	}

	public static void readPacketData(Map<Faction,Integer> factions, JsonElement reputationStandingsData) {
		ClientHandler.standings = new ReputationStandings(reputationStandingsData);
		for(Faction faction : factions.keySet()) {
			CLIENT_FACTIONS.put(faction.getID(),faction);
			CLIENT_FACTIONS_REPUTATION.put(faction, factions.get(faction));
		}
	}
}