package mods.thecomputerizer.reputation.capability.placedcontainer;

import net.minecraft.nbt.CompoundTag;

public interface IPlacedContainer {

    boolean getCheck();
    void setCheck(boolean check);
    CompoundTag writeTag(CompoundTag tag);
    void readTag(CompoundTag tag);
}
