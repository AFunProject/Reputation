package mods.thecomputerizer.reputation.registry.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static mods.thecomputerizer.reputation.registry.BlockEntitiesRegistry.LEDGER_BOOK_ENTITY;

public class LedgerBookEntity extends BlockEntity {

    public static <T> void tick(Level ignoredLevel, BlockPos ignoredPos, BlockState ignoredState, T book) {
        ((LedgerBookEntity)book).tick();
    }

    private int tickCounter = 0;
    
    public LedgerBookEntity(BlockPos pos, BlockState state) {
        this(LEDGER_BOOK_ENTITY.get(),pos,state);
    }

    protected LedgerBookEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type,pos,state);
    }

    private void tick() {
        if(this.tickCounter<10) this.tickCounter++;
    }

    public boolean canUse() {
        return this.tickCounter>=10;
    }

    public void setCooldown() {
        this.tickCounter = 0;
    }

    @Override public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
    }

    @Override public CompoundTag serializeNBT() {
        return super.serializeNBT();
    }
}