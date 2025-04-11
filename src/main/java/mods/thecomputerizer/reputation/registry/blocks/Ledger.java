package mods.thecomputerizer.reputation.registry.blocks;

import mods.thecomputerizer.reputation.registry.blockentities.LedgerEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

import static mods.thecomputerizer.reputation.registry.BlockEntitiesRegistry.LEDGER_ENTITY;
import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;
import static net.minecraft.world.InteractionResult.CONSUME;
import static net.minecraft.world.InteractionResult.SUCCESS;
import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;
import static net.minecraft.world.level.block.RenderShape.MODEL;

@MethodsReturnNonnullByDefault @ParametersAreNonnullByDefault
public class Ledger extends BaseEntityBlock {
    
    public Ledger(Properties properties) {
        super(properties);
    }
    
    @SuppressWarnings("deprecation")
    @Override public RenderShape getRenderShape(BlockState state) {
        return MODEL;
    }

    @Override protected void createBlockStateDefinition(Builder<Block,BlockState> builder) {
        builder.add(FACING);
    }
    
    protected static <T extends BlockEntity> @Nullable BlockEntityTicker<T> createTicker(Level level,
            BlockEntityType<T> type, BlockEntityType<T> ledgerType) {
        return level.isClientSide ? null : createTickerHelper(type,ledgerType,LedgerEntity::tick);
    }
    
    @SuppressWarnings("deprecation")
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        final boolean ns = state.getValue(FACING)==NORTH || state.getValue(FACING)==SOUTH;
        double x = ns ? 2d : 0d;
        double z = ns ? 0d : 2d;
        return Block.box(2d-x,0d,2d-z,14d+x,10d,14d+z);
    }
    
    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING,ctx.getHorizontalDirection().getOpposite());
    }
    
    @SuppressWarnings("unchecked")
    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return createTicker(level,type,(BlockEntityType<T>)LEDGER_ENTITY.get());
    }
    
    @SuppressWarnings("deprecation")
    @Override public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
    
    @Override public  @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LedgerEntity(pos,state);
    }
    
    @SuppressWarnings("deprecation")
    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if(!state.is(newState.getBlock())) {
            BlockEntity blockentity = level.getBlockEntity(pos);
            if(blockentity instanceof Container container) {
                Containers.dropContents(level,pos,container);
                level.updateNeighbourForOutputSignal(pos,this);
            }
            super.onRemove(state,level,pos,newState,isMoving);
        }
    }
    
    @SuppressWarnings("deprecation")
    @Override public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));
    }
    
    @SuppressWarnings("deprecation")
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if(level.isClientSide) return SUCCESS;
        BlockEntity entity = level.getBlockEntity(pos);
        if(entity instanceof LedgerEntity) player.openMenu(state.getMenuProvider(level,pos));
        return CONSUME;
    }
}