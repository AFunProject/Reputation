package mods.thecomputerizer.reputation.common.objects.blocks;

import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.common.objects.blockentities.LedgerBookEntity;
import mods.thecomputerizer.reputation.common.objects.items.FactionCurrencyBag;
import mods.thecomputerizer.reputation.common.registration.BlockEntities;
import mods.thecomputerizer.reputation.common.registration.Sounds;
import mods.thecomputerizer.reputation.util.HelperMethods;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        else {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof LedgerBookEntity ledgerBookEntity) {
                if (ledgerBookEntity.canUse() && level instanceof ServerLevel) {
                    ItemStack stack = player.getMainHandItem();
                    CompoundTag tag = stack.getOrCreateTag();
                    if (stack.getItem() instanceof FactionCurrencyBag && player.getOffhandItem().getItem() == Items.INK_SAC
                            && tag.contains("factionID")) {
                        Faction faction = ReputationHandler.getFaction(new ResourceLocation(tag.getString("factionID")));
                        if(Objects.nonNull(faction) && !tag.contains("signed")) {
                            float factor = 2f;
                            if (HelperMethods.getNearEntitiesOfFaction((ServerLevel) level, player, faction, 8).isEmpty()) {
                                player.sendMessage(new TextComponent("The book acknowledges the tribute"), Util.NIL_UUID);
                                factor = 1f;
                                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), Sounds.LEDGER_SIGN.get(),
                                        SoundSource.BLOCKS, 1f, Mth.randomBetween(ReputationHandler.RANDOM, 0.88f, 1.12f), false);
                            } else {
                                player.sendMessage(new TextComponent("The book acknowledges the tribute and the presence of a fitting 3rd party"), Util.NIL_UUID);
                                if (!player.getMainHandItem().getOrCreateTag().contains("Enchantments")) {
                                    player.getMainHandItem().getOrCreateTag().put("Enchantments", new ListTag());
                                    CompoundTag compoundtag = new CompoundTag();
                                    compoundtag.putString("id", "signed");
                                    compoundtag.putShort("lvl", (short) 1);
                                    player.getMainHandItem().getOrCreateTag().getList("Enchantments", 10).add(compoundtag);
                                    level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), Sounds.LEDGER_SIGN.get(),
                                            SoundSource.BLOCKS, 1f, Mth.randomBetween(ReputationHandler.RANDOM, 0.88f, 1.12f), false);
                                }
                            }
                            player.getMainHandItem().getOrCreateTag().putFloat("signed", factor);
                            player.getOffhandItem().shrink(1);
                            ledgerBookEntity.setCooldown();
                            return InteractionResult.CONSUME_PARTIAL;
                        }
                    } else {
                        Faction faction = ReputationHandler.getFactionFromCurrency(player.getMainHandItem().getItem());
                        if(Objects.isNull(faction))
                            faction = ReputationHandler.getFactionFromCurrency(player.getOffhandItem().getItem());
                        if(Objects.nonNull(faction)) {
                            String builder = "The writing in the book seems to be shifting but briefly settles on some numbers as you look at it: \n" +
                                    faction.getName() + " -> " + ReputationHandler.getReputation(player, faction);
                            player.sendMessage(new TextComponent(builder), Util.NIL_UUID);
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
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Block.box(3d,0d,3d,12d,4d,12d);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
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
    @Nullable
    public  <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state,
                                                                   @NotNull BlockEntityType<T> type) {
        return createTicker(level, type, BlockEntities.LEDGER_BOOK_ENTITY.get());
    }

    @Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level,
                                                                               @NotNull BlockEntityType<T> type,
                                                                               BlockEntityType<? extends LedgerBookEntity> bookType) {
        return level.isClientSide ? null : createTickerHelper(type, bookType, LedgerBookEntity::tick);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new LedgerBookEntity(pos,state);
    }
}
