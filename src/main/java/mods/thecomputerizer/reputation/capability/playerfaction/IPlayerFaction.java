package mods.thecomputerizer.reputation.capability.playerfaction;

import mods.thecomputerizer.reputation.capability.Faction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public interface IPlayerFaction {

    boolean isPlayerAttached(Player player);
    boolean addPlayer(Player player);
    boolean removePlayer(Player player);
    Faction getFaction();
    CompoundTag writeTag(CompoundTag tag);
    void readTag(CompoundTag tag);
}
