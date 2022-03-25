package mods.thecomputerizer.reputation.api.capability;

import net.minecraft.nbt.CompoundTag;

public interface IPlacedContainer {

    boolean getCheck();

    void setCheck(boolean check);

    CompoundTag writeNBT(CompoundTag nbt);

    void readNBT(CompoundTag nbt);
}
