package mods.thecomputerizer.reputation.common.objects.blocks;

import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.common.event.WorldEvents;
import mods.thecomputerizer.reputation.common.objects.entities.LedgerEntity;
import mods.thecomputerizer.reputation.common.objects.items.FactionCurrencyBag;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Random;

@SuppressWarnings("deprecation")
public class Ledger extends BaseEntityBlock implements SimpleWaterloggedBlock {

    private boolean checked;
    private LedgerEntity entity;
    private final HashMap<Player, HashMap<Faction, Integer>> depositedBags;
    public Ledger(Properties properties) {
        super(properties);
        this.checked = false;
        this.depositedBags = new HashMap<>();
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        else {
            if(hand==InteractionHand.MAIN_HAND && player.getMainHandItem().getItem() instanceof FactionCurrencyBag) {
                if(player.getMainHandItem().getOrCreateTag().contains("Signed")) {
                    Faction faction = ReputationHandler.FACTION_CURRENCY_MAP.get(ForgeRegistries.ITEMS.getValue(new ResourceLocation(player.getMainHandItem().getOrCreateTag().getCompound("Item").getString("id"))));
                    this.entity.addEmeraldBag(player.getMainHandItem());
                    depositedBags.putIfAbsent(player, new HashMap<>());
                    depositedBags.get(player).putIfAbsent(faction, 0);
                    depositedBags.get(player).put(faction, depositedBags.get(player).get(faction) + (int)player.getMainHandItem().getOrCreateTag().getFloat("Signed"));
                }
            }
            else if(hand==InteractionHand.OFF_HAND && player.getOffhandItem().getItem() instanceof FactionCurrencyBag) {
                if(player.getOffhandItem().getOrCreateTag().contains("Signed")) {
                    Faction faction = ReputationHandler.FACTION_CURRENCY_MAP.get(ForgeRegistries.ITEMS.getValue(new ResourceLocation(player.getMainHandItem().getOrCreateTag().getCompound("Item").getString("id"))));
                    this.entity.addEmeraldBag(player.getOffhandItem());
                    depositedBags.putIfAbsent(player, new HashMap<>());
                    depositedBags.get(player).putIfAbsent(faction, 0);
                    depositedBags.get(player).put(faction, depositedBags.get(player).get(faction) + (int)player.getMainHandItem().getOrCreateTag().getFloat("Signed"));
                }
            }
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        this.entity = new LedgerEntity(pos, state);
        return this.entity;
    }

    @Override
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull Random random) {
        if(WorldEvents.checkedLedgers) {
            if(!checked) {
                for(Player player : this.depositedBags.keySet()) for(Faction faction : this.depositedBags.get(player).keySet())
                    ReputationHandler.changeReputation(player,faction,this.depositedBags.get(player).get(faction));
                this.depositedBags.clear();
                checked = true;
            }
        } else checked = false;
    }
}
