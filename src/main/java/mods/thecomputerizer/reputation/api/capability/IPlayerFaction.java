package mods.thecomputerizer.reputation.api.capability;

import mods.thecomputerizer.reputation.api.Faction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public interface IPlayerFaction {

    boolean isPlayerAttached(Player player);

    void addPlayer(Player player);

    void removePlayer(Player player);

    Faction getFaction();

    CompoundTag writeNBT(CompoundTag nbt);

    void readNBT(CompoundTag nbt);

}
