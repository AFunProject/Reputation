package mods.thecomputerizer.reputation.registry.blocks;

import mods.thecomputerizer.reputation.ReputationRef;
import mods.thecomputerizer.reputation.capability.Faction;
import mods.thecomputerizer.reputation.capability.handlers.ReputationHandler;
import mods.thecomputerizer.reputation.registry.blockentities.LedgerBookEntity;
import mods.thecomputerizer.reputation.registry.items.FactionCurrencyBag;
import mods.thecomputerizer.reputation.util.HelperMethods;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import static mods.thecomputerizer.reputation.registry.BlockEntitiesRegistry.LEDGER_BOOK_ENTITY;
import static mods.thecomputerizer.reputation.registry.SoundRegistry.LEDGER_SIGN;
import static net.minecraft.Util.NIL_UUID;
import static net.minecraft.sounds.SoundSource.BLOCKS;
import static net.minecraft.world.InteractionResult.CONSUME_PARTIAL;
import static net.minecraft.world.InteractionResult.FAIL;
import static net.minecraft.world.InteractionResult.PASS;
import static net.minecraft.world.InteractionResult.SUCCESS;
import static net.minecraft.world.item.Items.INK_SAC;
import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;
import static net.minecraft.world.level.block.RenderShape.MODEL;

@MethodsReturnNonnullByDefault @ParametersAreNonnullByDefault
public class LedgerBook extends BaseEntityBlock {

    public LedgerBook(Properties properties) {
        super(properties);
    }

    @Override protected void createBlockStateDefinition(Builder<Block,BlockState> builder) {
        builder.add(FACING);
    }

    protected static <T extends BlockEntity> @Nullable BlockEntityTicker<T> createTicker(Level level,
            BlockEntityType<T> type, BlockEntityType<T> bookType) {
        return level.isClientSide ? null : createTickerHelper(type,bookType,LedgerBookEntity::tick);
    }
    
    @SuppressWarnings("deprecation")
    @Override public RenderShape getRenderShape(BlockState state) {
        return MODEL;
    }
    
    @SuppressWarnings("deprecation")
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Block.box(3d,0d,3d,12d,4d,12d);
    }
    
    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING,ctx.getHorizontalDirection().getOpposite());
    }
    
    @SuppressWarnings("unchecked")
    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return createTicker(level,type,(BlockEntityType<T>)LEDGER_BOOK_ENTITY.get());
    }
    
    protected InteractionResult interactionMsg(final InteractionResult result, final Player player,
            final Collection<Component> msgs) {
        for(Component msg : msgs) player.sendMessage(msg,NIL_UUID);
        return result;
    }
    
    @SuppressWarnings("deprecation")
    @Override public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LedgerBookEntity(pos,state);
    }
    
    @SuppressWarnings("deprecation")
    @Override public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));
    }
    
    @SuppressWarnings("deprecation")
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        final Collection<Component> msgs = new ArrayList<>();
        BlockEntity entity = level.getBlockEntity(pos);
        if(entity instanceof LedgerBookEntity ledgerBookEntity) {
            if(level.isClientSide) return interactionMsg(SUCCESS,player,msgs);
            ItemStack mainStack = player.getMainHandItem();
            ItemStack offStack = player.getOffhandItem();
            Item mainItem = mainStack.getItem();
            Item offItem = offStack.getItem();
            if(ledgerBookEntity.canUse() && level instanceof ServerLevel sLevel) {
                CompoundTag tag = mainStack.getOrCreateTag();
                if(mainItem instanceof FactionCurrencyBag && offItem==INK_SAC && tag.contains("factionID")) {
                    Faction faction = ReputationHandler.getFaction(tag.getString("factionID"));
                    if(Objects.nonNull(faction) && !tag.contains("signed")) {
                        float factor = 2f;
                        if(HelperMethods.getNearEntitiesOfFaction(sLevel,player,faction,8).isEmpty()) {
                            msgs.add(new TranslatableComponent("ledger_book.reputation.acknowledgement"));
                            factor = 1f;
                            level.playLocalSound(pos.getX(),pos.getY(),pos.getZ(),LEDGER_SIGN.get(),BLOCKS,1f,
                                                 ReputationRef.floatRand(0.88f,1.12f),false);
                        } else {
                            msgs.add(new TranslatableComponent("ledger_book.reputation.acknowledgement.extra"));
                            if(!tag.contains("Enchantments")) {
                                tag.put("Enchantments", new ListTag());
                                CompoundTag enchTag = new CompoundTag();
                                enchTag.putString("id","signed");
                                enchTag.putShort("lvl",(short) 1);
                                tag.getList("Enchantments",10).add(enchTag);
                                level.playLocalSound(pos.getX(),pos.getY(),pos.getZ(),LEDGER_SIGN.get(),BLOCKS,
                                                     1f,ReputationRef.floatRand(0.88f,1.12f),false);
                            }
                        }
                        tag.putFloat("signed", factor);
                        offStack.shrink(1);
                        ledgerBookEntity.setCooldown();
                        return interactionMsg(CONSUME_PARTIAL,player,msgs);
                    }
                } else {
                    Faction faction = ReputationHandler.getFactionFromCurrency(mainStack.getItem());
                    if(Objects.isNull(faction)) faction = ReputationHandler.getFactionFromCurrency(offStack.getItem());
                    if(Objects.nonNull(faction)) {
                        String arg1 = faction.getName();
                        int arg2 = ReputationHandler.getReputation(player,faction);
                        msgs.add(new TranslatableComponent("ledger_book.reputation.numbers",arg1,arg2));
                        ledgerBookEntity.setCooldown();
                    }
                }
                return interactionMsg(PASS,player,msgs);
            }
        }
        return interactionMsg(FAIL,player,msgs);
    }
}