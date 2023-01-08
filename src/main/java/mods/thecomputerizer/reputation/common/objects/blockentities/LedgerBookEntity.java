package mods.thecomputerizer.reputation.common.objects.blockentities;

import mods.thecomputerizer.reputation.common.registration.BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class LedgerBookEntity extends BlockEntity {

    @SuppressWarnings("unused")
    public static void tick(Level level, BlockPos pos, BlockState state, LedgerBookEntity bookEntity) {
        bookEntity.tick();
    }

    private int tickCounter = 0;

    protected LedgerBookEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
    }

    public LedgerBookEntity(BlockPos pos, BlockState state) {
        this(BlockEntities.LEDGER_BOOK_ENTITY.get(), pos, state);
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

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
    }

    @Override
    public CompoundTag serializeNBT() {
        return super.serializeNBT();
    }
}
