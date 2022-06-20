package mods.thecomputerizer.reputation.common.capability;

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
		if (!FACTION_IDS.containsKey(faction.getID().toString())) FACTION_IDS.put(faction.getID().toString(), faction.getDefaultRep());
		return FACTION_IDS.get(faction.getID().toString());
	}

	@Override
	public void setReputation(Player player, Faction faction, int reputation) {
		FACTIONS.put(faction, reputation);
		FACTION_IDS.put(faction.getID().toString(),reputation);
		if (player instanceof ServerPlayer) {
			PacketHandler.sendTo(new SyncReputationMessage(faction, reputation),(ServerPlayer)player);
		}
	}

	@Override
	public void changeReputation(Player player, Faction faction, int reputation) {
		int base = FACTION_IDS.containsKey(faction.getID().toString()) ? FACTION_IDS.get(faction.getID().toString()) : faction.getDefaultRep();
		FACTION_IDS.put(faction.getID().toString(), base + reputation);
		FACTIONS.put(faction, base + reputation);
		if (player instanceof ServerPlayer) {
			PacketHandler.sendTo(new SyncReputationMessage(faction, base + reputation),(ServerPlayer)player);
		}
	}

	@Override
	public CompoundTag writeNBT(CompoundTag nbt) {
		for (Faction entry : FACTIONS.keySet()) {
			if(!nbt.contains(entry.getID().toString())) {
				nbt.putInt(entry.getID().toString(), FACTIONS.get(entry));
			}
		}
		return nbt;
	}

	@Override
	public void readNBT(CompoundTag nbt) {
		FACTIONS = new HashMap<>();
		FACTION_IDS = new HashMap<>();
		for (Faction f : ReputationHandler.getFactionMap().values()) {
			if (nbt.contains(f.getID().toString())) {
				FACTIONS.putIfAbsent(f, nbt.getInt(f.getID().toString()));
				FACTION_IDS.putIfAbsent(f.getID().toString(), nbt.getInt(f.getID().toString()));
			} else {
				FACTIONS.putIfAbsent(f, f.getDefaultRep());
				FACTION_IDS.putIfAbsent(f.getID().toString(), f.getDefaultRep());
			}
		}
	}
}