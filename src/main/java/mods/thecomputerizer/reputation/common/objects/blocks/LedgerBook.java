package mods.thecomputerizer.reputation.common.objects.blocks;

import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.client.ClientHandler;
import mods.thecomputerizer.reputation.common.event.WorldEvents;
import mods.thecomputerizer.reputation.common.objects.items.FactionCurrencyBag;
import mods.thecomputerizer.reputation.common.registration.Sounds;
import mods.thecomputerizer.reputation.util.HelperMethods;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class LedgerBook extends Block {

    public int tick;

    public LedgerBook(Properties properties) {
        super(properties);
        WorldEvents.books.add(this);
        this.tick = 10;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if(this.tick>=10 && level instanceof ServerLevel) {
            if (player.getMainHandItem().getItem() instanceof FactionCurrencyBag && player.getOffhandItem().getItem().getRegistryName().toString().matches(Items.INK_SAC.getRegistryName().toString())
                &! player.getMainHandItem().getOrCreateTag().contains("Signed") && player.getMainHandItem().getOrCreateTag().contains("Item")
                && ForgeRegistries.ITEMS.getValue(new ResourceLocation(player.getMainHandItem().getOrCreateTag().getCompound("Item").getString("id")))!=null) {
                    float factor = 2f;
                    if (HelperMethods.getNearEntitiesOfFaction((ServerLevel) level, player, ReputationHandler.FACTION_CURRENCY_MAP.get(ForgeRegistries.ITEMS.getValue(new ResourceLocation(player.getMainHandItem().getOrCreateTag().getCompound("Item").getString("id")))),8).isEmpty()) {
                        player.sendMessage(new TextComponent("The book acknowledges the tribute"), Util.NIL_UUID);
                        factor = 1f;
                        ClientHandler.playPacketSound(Sounds.LEDGER_SIGN.get());
                    } else {
                        player.sendMessage(new TextComponent("The book acknowledges the tribute and the presence of a fitting 3rd party"), Util.NIL_UUID);
                        if (!player.getMainHandItem().getOrCreateTag().contains("Enchantments")) {
                            player.getMainHandItem().getOrCreateTag().put("Enchantments", new ListTag());
                            CompoundTag compoundtag = new CompoundTag();
                            compoundtag.putString("id", "signed");
                            compoundtag.putShort("lvl", (short) 1);
                            player.getMainHandItem().getOrCreateTag().getList("Enchantments", 10).add(compoundtag);
                            ClientHandler.playPacketSound(Sounds.LEDGER_SIGN.get());
                        }
                    }
                    player.getMainHandItem().getOrCreateTag().putFloat("Signed", factor);
                    player.getOffhandItem().shrink(1);
                    this.tick = 0;
                    return InteractionResult.CONSUME_PARTIAL;
            } else if(ReputationHandler.FACTION_CURRENCY_MAP.containsKey(player.getMainHandItem().getItem())) {
                Faction f = ReputationHandler.FACTION_CURRENCY_MAP.get(player.getMainHandItem().getItem());
                String builder = "The writing in the book seems to be shifting but briefly settles on some numbers as you look at it: \n" +
                        f.getName() + " -> " + ReputationHandler.getReputation(player, f);
                player.sendMessage(new TextComponent(builder), Util.NIL_UUID);
                this.tick = 0;
            } else if (ReputationHandler.FACTION_CURRENCY_MAP.containsKey(player.getOffhandItem().getItem())) {
                Faction f = ReputationHandler.FACTION_CURRENCY_MAP.get(player.getOffhandItem().getItem());
                String builder = "The writing in the book seems to be shifting but briefly settles on some numbers as you look at it: \n" +
                        f.getName() + " -> " + ReputationHandler.getReputation(player, f);
                player.sendMessage(new TextComponent(builder), Util.NIL_UUID);
                this.tick = 0;
            }
            return InteractionResult.PASS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return Block.box(3d,0d,3d,12d,4d,12d);
    }

    @Override
    public void destroy(@NotNull LevelAccessor pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState) {
        WorldEvents.books.remove(this);
    }

    @Override
    public void wasExploded(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Explosion pExplosion) {
        WorldEvents.books.remove(this);
    }

    public void tick() {
        if(this.tick<10) this.tick++;
    }
}
