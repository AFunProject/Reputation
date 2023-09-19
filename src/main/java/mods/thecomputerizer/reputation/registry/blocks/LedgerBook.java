package mods.thecomputerizer.reputation.registry.blocks;

import mods.thecomputerizer.reputation.Constants;
import mods.thecomputerizer.reputation.capability.Faction;
import mods.thecomputerizer.reputation.capability.handlers.ReputationHandler;
import mods.thecomputerizer.reputation.registry.BlockEntitiesRegistry;
import mods.thecomputerizer.reputation.registry.SoundRegistry;
import mods.thecomputerizer.reputation.registry.blockentities.LedgerBookEntity;
import mods.thecomputerizer.reputation.registry.items.FactionCurrencyBag;
import mods.thecomputerizer.reputation.util.HelperMethods;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

import javax.annotation.Nullable;
import java.util.Objects;


@SuppressWarnings("deprecation")
public class LedgerBook extends BaseEntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public LedgerBook(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult use(
            @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player,
            @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if(level.isClientSide) return InteractionResult.SUCCESS;
        else {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof LedgerBookEntity ledgerBookEntity) {
                ItemStack mainItem = player.getMainHandItem();
                ItemStack offItem = player.getOffhandItem();
                if(ledgerBookEntity.canUse() && level instanceof ServerLevel sLevel) {
                    CompoundTag tag = mainItem.getOrCreateTag();
                    if(mainItem.getItem() instanceof FactionCurrencyBag && offItem.getItem()==Items.INK_SAC &&
                            tag.contains("factionID")) {
                        Faction faction = ReputationHandler.getFaction(new ResourceLocation(tag.getString("factionID")));
                        if(Objects.nonNull(faction) && !tag.contains("signed")) {
                            float factor = 2f;
                            if(HelperMethods.getNearEntitiesOfFaction(sLevel,player,faction,8).isEmpty()) {
                                player.sendMessage(new TranslatableComponent("ledger_book.reputation.acknowledgement"),Util.NIL_UUID);
                                factor = 1f;
                                level.playLocalSound(pos.getX(),pos.getY(),pos.getZ(),SoundRegistry.LEDGER_SIGN.get(),
                                        SoundSource.BLOCKS,1f,Constants.floatRand(0.88f, 1.12f),false);
                            } else {
                                player.sendMessage(new TranslatableComponent("ledger_book.reputation.acknowledgement.extra"),Util.NIL_UUID);
                                if(!tag.contains("Enchantments")) {
                                    tag.put("Enchantments", new ListTag());
                                    CompoundTag enchTag = new CompoundTag();
                                    enchTag.putString("id", "signed");
                                    enchTag.putShort("lvl", (short) 1);
                                    tag.getList("Enchantments", 10).add(enchTag);
                                    level.playLocalSound(pos.getX(),pos.getY(),pos.getZ(),SoundRegistry.LEDGER_SIGN.get(),
                                            SoundSource.BLOCKS,1f,Constants.floatRand(0.88f, 1.12f),false);
                                }
                            }
                            tag.putFloat("signed", factor);
                            offItem.shrink(1);
                            ledgerBookEntity.setCooldown();
                            return InteractionResult.CONSUME_PARTIAL;
                        }
                    } else {
                        Faction faction = ReputationHandler.getFactionFromCurrency(mainItem.getItem());
                        if(Objects.isNull(faction))
                            faction = ReputationHandler.getFactionFromCurrency(offItem.getItem());
                        if(Objects.nonNull(faction)) {
                            player.sendMessage(new TranslatableComponent("ledger_book.reputation.numbers",
                                    faction.getName(),ReputationHandler.getReputation(player,faction)),Util.NIL_UUID);
                            ledgerBookEntity.setCooldown();
                        }
                    }
                    return InteractionResult.PASS;
                }
            }
            return InteractionResult.FAIL;
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos,
                                        @NotNull CollisionContext ctx) {
        return Block.box(3d,0d,3d,12d,4d,12d);
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return createTicker(level,type,BlockEntitiesRegistry.LEDGER_BOOK_ENTITY.get());
    }

    protected static @Nullable <T extends BlockEntity> BlockEntityTicker<T> createTicker(
            Level level, @NotNull BlockEntityType<T> type, BlockEntityType<? extends LedgerBookEntity> bookType) {
        return level.isClientSide ? null :
                createTickerHelper(type,bookType,(level1,pos,state,book) -> LedgerBookEntity.tick(book));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new LedgerBookEntity(pos,state);
    }
}
