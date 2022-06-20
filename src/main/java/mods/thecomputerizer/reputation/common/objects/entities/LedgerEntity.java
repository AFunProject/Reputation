package mods.thecomputerizer.reputation.common.objects.entities;

import mods.thecomputerizer.reputation.common.registration.BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class LedgerEntity extends RandomizableContainerBlockEntity implements LidBlockEntity {

    public NonNullList<ItemStack> items;

    protected LedgerEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public LedgerEntity(BlockPos pos, BlockState state) {
        this(BlockEntities.LEDGER_ENTITY.get(), pos, state);
    }

    @Override
    protected @NotNull NonNullList<ItemStack> getItems() {
        return this.items;
    }

    public void addEmeraldBag(ItemStack bag) {
        if(getItems().size()<getContainerSize()) {
            NonNullList<ItemStack> bags = getItems();
            bags.add(bag);
            setItems(bags);
            bag.setCount(0);
        }
    }


    @Override
    protected void setItems(@NotNull NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return new TextComponent("ledger_entity");
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pInventory) {
        return null;
    }

    @Override
    public int getContainerSize() {
        return 10;
    }

    @Override
    public float getOpenNess(float pPartialTicks) {
        return 0;
    }
}
