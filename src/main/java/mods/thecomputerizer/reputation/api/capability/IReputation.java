package mods.thecomputerizer.reputation.api.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import mods.thecomputerizer.reputation.api.Faction;

import java.util.HashMap;

public interface IReputation {

	HashMap<Faction, Integer> allReputations();

	int getReputation(Faction faction);

	void setReputation(Player player, Faction faction, int reputation);

	void changeReputation(Player player, Faction faction, int reputation);

	CompoundTag writeNBT(CompoundTag nbt);

	void readNBT(CompoundTag nbt);

}
