package mods.thecomputerizer.reputation.capability.reputation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import mods.thecomputerizer.reputation.capability.Faction;


import java.util.Map;

public interface IReputation {

	Map<Faction, Integer> allReputations();
	int getReputation(Faction faction);
	void setReputation(Player player, Faction faction, int reputation);
	void changeReputation(Player player, Faction faction, int reputation);
	CompoundTag writeTag(CompoundTag tag);
	void readTag(CompoundTag tag);
}
