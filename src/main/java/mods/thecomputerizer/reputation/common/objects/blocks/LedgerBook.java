package mods.thecomputerizer.reputation.common.objects.blocks;

import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.common.objects.items.FactionCurrencyBag;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

@SuppressWarnings("deprecation")
public class LedgerBook extends Block {

    public boolean canUse;

    public LedgerBook(Properties properties) {
        super(properties);
        this.canUse = true;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if(this.canUse) {
            if (player.getMainHandItem().getItem() instanceof FactionCurrencyBag && player.getOffhandItem().getItem().getRegistryName().toString().matches(Items.INK_SAC.getRegistryName().toString())
                &! player.getMainHandItem().getOrCreateTag().contains("Signed") && player.getMainHandItem().getOrCreateTag().contains("Item")
                && ForgeRegistries.ITEMS.getValue(new ResourceLocation(player.getMainHandItem().getOrCreateTag().getCompound("Item").getString("id")))!=null) {
                    float factor = 2f;
                    if (HelperMethods.getSeenEntitiesOfFaction(player.getBrain(), ReputationHandler.FACTION_CURRENCY_MAP.get(ForgeRegistries.ITEMS.getValue(new ResourceLocation(player.getMainHandItem().getOrCreateTag().getCompound("Item").getString("id"))))).isEmpty()) {
                        player.sendMessage(new TextComponent("The book acknowledges the tribute"), Util.NIL_UUID);
                        factor = 1f;
                    } else {
                        player.sendMessage(new TextComponent("The book acknowledges the tribute and the presence of a fitting 3rd party"), Util.NIL_UUID);
                        if (!player.getMainHandItem().getOrCreateTag().contains("Enchantments")) {
                            player.getMainHandItem().getOrCreateTag().put("Enchantments", new ListTag());
                            CompoundTag compoundtag = new CompoundTag();
                            compoundtag.putString("id", "signed");
                            compoundtag.putShort("lvl", (short) 1);
                            player.getMainHandItem().getOrCreateTag().getList("Enchantments", 10).add(compoundtag);
                        }
                    }
                    player.getMainHandItem().getOrCreateTag().putFloat("Signed", factor);
                    player.getOffhandItem().shrink(1);
                    this.canUse = false;
                    return InteractionResult.CONSUME_PARTIAL;
            } else {
                StringBuilder builder = new StringBuilder();
                builder.append("The writing in the book seems to be shifting but briefly settles on some numbers as you look at it: \n");
                for (Faction f : ReputationHandler.getFactionMap().values())
                    builder.append(f.getName()).append(" -> ").append(ReputationHandler.getReputation(player, f));
                player.sendMessage(new TextComponent(builder.toString()), Util.NIL_UUID);
            }
            this.canUse = false;
            return InteractionResult.PASS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull Random random) {
        if (!this.canUse) this.canUse = true;
    }
}
