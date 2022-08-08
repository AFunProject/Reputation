package mods.thecomputerizer.reputation.common.objects.blocks;

import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.client.ClientHandler;
import mods.thecomputerizer.reputation.common.event.WorldEvents;
import mods.thecomputerizer.reputation.common.objects.items.FactionCurrencyBag;
import mods.thecomputerizer.reputation.common.registration.Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

@SuppressWarnings("deprecation")
public class Ledger extends Block {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private boolean checked;
    private final HashMap<Player, HashMap<Faction, Integer>> depositedBags;
    public Ledger(Properties properties) {
        super(properties);
        WorldEvents.ledgers.add(this);
        this.checked = false;
        this.depositedBags = new HashMap<>();
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        else {
            if(hand==InteractionHand.MAIN_HAND && player.getMainHandItem().getItem() instanceof FactionCurrencyBag) {
                if(player.getMainHandItem().getTag().contains("Signed")) {
                    Faction faction = ReputationHandler.FACTION_CURRENCY_MAP.get(ForgeRegistries.ITEMS.getValue(new ResourceLocation(player.getMainHandItem().getTag().getCompound("Item").getString("id"))));
                    depositedBags.putIfAbsent(player, new HashMap<>());
                    depositedBags.get(player).putIfAbsent(faction, 0);
                    depositedBags.get(player).put(faction, depositedBags.get(player).get(faction) + (int)player.getMainHandItem().getTag().getFloat("Signed"));
                    player.getMainHandItem().shrink(1);
                    ClientHandler.playPacketSound(Sounds.LEDGER_PLACE.get());
                }
            }
            else if(hand==InteractionHand.OFF_HAND && player.getOffhandItem().getItem() instanceof FactionCurrencyBag) {
                if(player.getOffhandItem().getTag().contains("Signed")) {
                    Faction faction = ReputationHandler.FACTION_CURRENCY_MAP.get(ForgeRegistries.ITEMS.getValue(new ResourceLocation(player.getMainHandItem().getTag().getCompound("Item").getString("id"))));
                    depositedBags.putIfAbsent(player, new HashMap<>());
                    depositedBags.get(player).putIfAbsent(faction, 0);
                    depositedBags.get(player).put(faction, depositedBags.get(player).get(faction) + (int)player.getMainHandItem().getTag().getFloat("Signed"));
                    player.getMainHandItem().shrink(1);
                    ClientHandler.playPacketSound(Sounds.LEDGER_PLACE.get());
                }
            }
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        if(state.getValue(FACING)==Direction.NORTH || state.getValue(FACING)==Direction.SOUTH)
            return Block.box(0d,0d,2d,16d,10d,14d);
        return Block.box(2d,0d,0d,14d,10d,16d);
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
    public void destroy(@NotNull LevelAccessor pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState) {
        WorldEvents.ledgers.remove(this);
    }

    @Override
    public void wasExploded(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Explosion pExplosion) {
        WorldEvents.ledgers.remove(this);
    }

    public void tick() {
        if(WorldEvents.checkedLedgers) {
            if(!checked) {
                for(Player player : this.depositedBags.keySet()) {
                    for(Faction faction : this.depositedBags.get(player).keySet())
                        ReputationHandler.changeReputation(player,faction,this.depositedBags.get(player).get(faction));
                }
                this.depositedBags.clear();
                checked = true;
            }
        } else checked = false;
    }
}
