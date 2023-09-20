package mods.thecomputerizer.reputation.capability.reputation;

import mods.thecomputerizer.reputation.capability.Faction;
import mods.thecomputerizer.reputation.capability.handlers.ReputationHandler;
import mods.thecomputerizer.reputation.network.PacketSyncReputation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ReputationCapability implements IReputation {

	private Map<Faction, Integer> FACTIONS = new HashMap<>();
	private Map<String, Integer> FACTION_IDS = new HashMap<>();

	@Override
	public Map<Faction, Integer> allReputations() {
		return FACTIONS;
	}

	@Override
	public int getReputation(Faction faction) {
		if(Objects.isNull(faction)) return 0;
		if (!FACTION_IDS.containsKey(faction.getID().toString())) FACTION_IDS.put(faction.getID().toString(),faction.getDefaultRep());
		return FACTION_IDS.get(faction.getID().toString());
	}

	@Override
	public void setReputation(Player p, Faction faction, int reputation) {
		if(Objects.isNull(p) || Objects.isNull(faction)) return;
		FACTIONS.put(faction, reputation);
		FACTION_IDS.put(faction.getID().toString(),reputation);
		if(p instanceof ServerPlayer player)
			new PacketSyncReputation(faction.getID(),reputation).addPlayers(player).send();
	}

	@Override
	public void changeReputation(Player p, Faction faction, int reputation) {
		if(Objects.isNull(p) || Objects.isNull(faction)) return;
		int base = FACTION_IDS.containsKey(faction.getID().toString()) ? FACTION_IDS.get(faction.getID().toString()) : faction.getDefaultRep();
		FACTION_IDS.put(faction.getID().toString(), base + reputation);
		FACTIONS.put(faction, base + reputation);
		if(p instanceof ServerPlayer player)
			new PacketSyncReputation(faction.getID(),base+reputation).addPlayers(player).send();
	}

	@Override
	public CompoundTag writeTag(CompoundTag tag) {
		for(Faction entry : FACTIONS.keySet())
			if(!tag.contains(entry.getID().toString()))
				tag.putInt(entry.getID().toString(), FACTIONS.get(entry));
		return tag;
	}

	@Override
	public void readTag(CompoundTag tag) {
		FACTIONS = new HashMap<>();
		FACTION_IDS = new HashMap<>();
		for (Faction f : ReputationHandler.getFactionMap().values()) {
			if (tag.contains(f.getID().toString())) {
				FACTIONS.put(f,tag.getInt(f.getID().toString()));
				FACTION_IDS.put(f.getID().toString(),tag.getInt(f.getID().toString()));
			} else {
				FACTIONS.put(f,f.getDefaultRep());
				FACTION_IDS.put(f.getID().toString(),f.getDefaultRep());
			}
		}
	}
}