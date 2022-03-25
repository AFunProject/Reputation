package mods.thecomputerizer.reputation.common.capability;

import mods.thecomputerizer.reputation.api.capability.IPlacedContainer;
import net.minecraft.nbt.CompoundTag;

public class PlacedContainer implements IPlacedContainer {

    private boolean check = true;

    @Override
    public boolean getCheck() {
        return this.check;
    }

    @Override
    public void setCheck(boolean check) {
        this.check = check;
    }

    @Override
    public CompoundTag writeNBT(CompoundTag nbt) {
        nbt.putBoolean("check_placed_by_player", this.check);
        return nbt;
    }

    @Override
    public void readNBT(CompoundTag nbt) {
        if (nbt.contains("check_placed_by_player")) check = nbt.getBoolean("check_placed_by_player");
    }
}
