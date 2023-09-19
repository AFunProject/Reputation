package mods.thecomputerizer.reputation.registry.blocks;

import mods.thecomputerizer.reputation.registry.blockentities.LedgerEntity;
import mods.thecomputerizer.reputation.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class Ledger extends BaseEntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public Ledger(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult use(
            @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player,
            @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        else {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof LedgerEntity)
                player.openMenu(state.getMenuProvider(level,pos));
            return InteractionResult.PASS;
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos,
                                        @NotNull CollisionContext ctx) {
        if(state.getValue(FACING)==Direction.NORTH || state.getValue(FACING)==Direction.SOUTH)
            return Block.box(0d,0d,2d,16d,10d,14d);
        return Block.box(2d,0d,0d,14d,10d,16d);
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState,
                         boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockentity = level.getBlockEntity(pos);
            if (blockentity instanceof Container container) {
                Containers.dropContents(level,pos,container);
                level.updateNeighbourForOutputSignal(pos,this);
            }
            super.onRemove(state,level,pos,newState,isMoving);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING,ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new LedgerEntity(pos,state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level, @NotNull BlockState state,@NotNull BlockEntityType<T> type) {
        return createTicker(level, type, BlockEntitiesRegistry.LEDGER_ENTITY.get());
    }

   @Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> createTicker(
            Level level, @NotNull BlockEntityType<T> type, BlockEntityType<? extends LedgerEntity> ledgerType) {
        return level.isClientSide ? null :
                createTickerHelper(type,ledgerType,(level1,pos,state,ledger) -> LedgerEntity.tick(level1,pos,ledger));
    }
}
