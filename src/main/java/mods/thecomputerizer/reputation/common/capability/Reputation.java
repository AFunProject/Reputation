package mods.thecomputerizer.reputation.common.capability;

import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.api.capability.IReputation;
import mods.thecomputerizer.reputation.common.network.PacketHandler;
import mods.thecomputerizer.reputation.common.network.SyncReputationMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Reputation implements IReputation {

	private final Map<Faction, Integer> FACTIONS = new HashMap<>();

	@Override
	public int getReputation(Faction faction) {
		if (!FACTIONS.containsKey(faction)) FACTIONS.put(faction, faction.getDefaultRep());
		return FACTIONS.get(faction);
	}

	@Override
	public void setReputation(Player player, Faction faction, int reputation) {
		FACTIONS.put(faction, reputation);
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER && player instanceof ServerPlayer) {
			PacketHandler.sendTo(new SyncReputationMessage(faction, reputation),(ServerPlayer)player);
		}
	}

	@Override
	public void changeReputation(Player player, Faction faction, int reputation) {
		int base = FACTIONS.containsKey(faction) ? FACTIONS.get(faction) : faction.getDefaultRep();
		FACTIONS.put(faction, base + reputation);
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER && player instanceof ServerPlayer) {
			PacketHandler.sendTo(new SyncReputationMessage(faction, base + reputation),(ServerPlayer)player);
		}
	}

	@Override
	public CompoundTag writeNBT(CompoundTag nbt) {
		for (Entry<Faction, Integer> entry : FACTIONS.entrySet()) {
			nbt.putInt(entry.getKey().getName().toString(), entry.getValue());
		}
		return nbt;
	}

	@Override
	public void readNBT(CompoundTag nbt) {
		for (Faction f : ReputationHandler.getFactions()) {
			if (nbt.contains(f.getName().toString())) {
				FACTIONS.put(f, nbt.getInt(f.getName().toString()));
			}
		}
	}
}