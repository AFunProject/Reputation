package mods.thecomputerizer.reputation.capability.placedcontainer;

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
    public CompoundTag writeTag(CompoundTag tag) {
        tag.putBoolean("check_placed_by_player", this.check);
        return tag;
    }

    @Override
    public void readTag(CompoundTag tag) {
        if(tag.contains("check_placed_by_player"))
            this.check = tag.getBoolean("check_placed_by_player");
    }
}
