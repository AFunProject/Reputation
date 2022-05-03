package mods.thecomputerizer.reputation.common.capability;

import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.api.capability.IReputation;
import mods.thecomputerizer.reputation.common.network.PacketHandler;
import mods.thecomputerizer.reputation.common.network.SyncReputationMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;

public class ReputationCapability implements IReputation {

	private HashMap<Faction, Integer> FACTIONS = new HashMap<>();
	private HashMap<String, Integer> FACTION_IDS = new HashMap<>();

	@Override
	public HashMap<Faction, Integer> allReputations() {
		return FACTIONS;
	}

	@Override
	public int getReputation(Faction faction) {
		if (!FACTION_IDS.containsKey(faction.getName().toString())) FACTION_IDS.put(faction.getName().toString(), faction.getDefaultRep());
		return FACTION_IDS.get(faction.getName().toString());
	}

	@Override
	public void setReputation(Player player, Faction faction, int reputation) {
		FACTIONS.put(faction, reputation);
		FACTION_IDS.put(faction.getName().toString(),reputation);
		if (player instanceof ServerPlayer) {
			PacketHandler.sendTo(new SyncReputationMessage(faction, reputation),(ServerPlayer)player);
		}
	}

	@Override
	public void changeReputation(Player player, Faction faction, int reputation) {
		int base = FACTION_IDS.containsKey(faction.getName().toString()) ? FACTION_IDS.get(faction.getName().toString()) : faction.getDefaultRep();
		Reputation.logInfo("base: "+FACTION_IDS.get(faction.getName().toString())+" faction: "+faction.getName());
		FACTION_IDS.put(faction.getName().toString(), base + reputation);
		FACTIONS.put(faction, base + reputation);
		if (player instanceof ServerPlayer) {
			PacketHandler.sendTo(new SyncReputationMessage(faction, base + reputation),(ServerPlayer)player);
		}
	}

	@Override
	public CompoundTag writeNBT(CompoundTag nbt) {
		Reputation.logInfo("writing nbt");
		for (Faction entry : FACTIONS.keySet()) {
			if(!nbt.contains(entry.getName().toString())) {
				nbt.putInt(entry.getName().toString(), FACTIONS.get(entry));
				Reputation.logInfo("writing cap int for " + entry.getName().toString() + ": " + nbt.getInt(entry.getName().toString()));
			}
		}
		return nbt;
	}

	@Override
	public void readNBT(CompoundTag nbt) {
		Reputation.logInfo("reading nbt");
		FACTIONS = new HashMap<>();
		FACTION_IDS = new HashMap<>();
		for (Faction f : ReputationHandler.getServerFactions()) {
			if (nbt.contains(f.getName().toString())) {
				FACTIONS.putIfAbsent(f, nbt.getInt(f.getName().toString()));
				FACTION_IDS.putIfAbsent(f.getName().toString(), nbt.getInt(f.getName().toString()));
				Reputation.logInfo("Reading cap int for " + f.getName().toString() + ": " + nbt.getInt(f.getName().toString()));
			} else {
				Reputation.logInfo("reading nbt factions default " + f.getName().toString());
				FACTIONS.putIfAbsent(f, f.getDefaultRep());
				FACTION_IDS.putIfAbsent(f.getName().toString(), f.getDefaultRep());
			}
		}
	}
}