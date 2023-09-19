package mods.thecomputerizer.reputation.registry.blockentities;

import mods.thecomputerizer.reputation.Constants;
import mods.thecomputerizer.reputation.capability.Faction;
import mods.thecomputerizer.reputation.capability.handlers.ReputationHandler;
import mods.thecomputerizer.reputation.registry.BlockEntitiesRegistry;
import mods.thecomputerizer.reputation.registry.ItemRegistry;
import mods.thecomputerizer.reputation.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class LedgerEntity extends BaseContainerBlockEntity implements LidBlockEntity {

    public static void tick(Level level, BlockPos pos, LedgerEntity ledger) {
        ledger.tick(level,pos);
    }

    protected NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private int cooldownTimer = 0;
    protected LedgerEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type,pos,state);
    }
    public LedgerEntity(BlockPos pos, BlockState state) {
        this(BlockEntitiesRegistry.LEDGER_ENTITY.get(),pos,state);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.items = NonNullList.withSize(this.getContainerSize(),ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag,this.items);
        if(tag.contains("ledger_cooldown"))
            this.cooldownTimer = tag.getInt("ledger_cooldown");
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag,this.items);
        tag.putInt("ledger_cooldown",this.cooldownTimer);
    }


    private void tick(Level level, BlockPos pos) {
        if(this.cooldownTimer>0) {
            this.cooldownTimer--;
            this.setCustomName(new TranslatableComponent("ledger.reputation.checked"));
        } else {
            long time = level.dayTime();
            if(time>=12000 && time<=13000) runCollection(level, pos);
        }
    }

    protected void runCollection(Level level, BlockPos pos) {
        for(ItemStack stack : this.items) {
            if(!stack.isEmpty() && stack.getItem()==ItemRegistry.FACTION_BAG.get()) {
                CompoundTag tag = stack.getOrCreateTag();
                if(tag.contains("signed") && tag.contains("factionID") && tag.contains("playerUUID")) {
                    Player player = level.getPlayerByUUID(tag.getUUID("playerUUID"));
                    if(Objects.nonNull(player)) {
                        Faction faction = ReputationHandler.getFaction(new ResourceLocation(tag.getString("factionID")));
                        int amount = (int)tag.getFloat("signed");
                        ReputationHandler.changeReputation(player, faction, amount);
                    }
                }
            }
        }
        level.playLocalSound(pos.getX(),pos.getY(),pos.getZ(),SoundRegistry.LEDGER_PLACE.get(),SoundSource.BLOCKS,
                1f,Constants.floatRand(0.88f,1.12f),false);
        this.clearContent();
        this.setCustomName(new TranslatableComponent("ledger.reputation.checked"));
        this.cooldownTimer = 2000;
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return new TranslatableComponent("ledger.reputation.container");
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory) {
        return new Menu(MenuType.GENERIC_9x3,containerId,this,inventory);
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        for(ItemStack stack : this.items)
            if(!stack.isEmpty()) return false;
        return true;
    }

    @Override
    public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
        return false;
    }

    @Override
    public @NotNull ItemStack getItem(int index) {
        return this.items.get(index);
    }

    @Override
    public @NotNull ItemStack removeItem(int index, int count) {
        return getItem(index);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int index) {
        return getItem(index);
    }

    @Override
    public void setItem(int index, @NotNull ItemStack stack) {
        this.items.set(index,stack);
        if (!stack.isEmpty() && stack.getCount()>this.getMaxStackSize())
            stack.setCount(this.getMaxStackSize());
        this.setChanged();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        BlockPos pos = this.worldPosition;
        return Objects.nonNull(this.level) && this.level.getBlockEntity(pos)==this && player.distanceToSqr(
                (double)pos.getX()+0.5d,(double)pos.getY()+0.5d,(double)pos.getZ()+0.5d)<=64d;
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.setChanged();
    }

    @Override
    public float getOpenNess(float pPartialTicks) {
        return 0;
    }

    private static class Menu extends AbstractContainerMenu {

        private final LedgerEntity container;

        protected Menu(@Nullable MenuType<?> menuType, int containerId, LedgerEntity container, Inventory inventory) {
            super(menuType,containerId);
            this.container = container;
            for(int i=0; i<3; ++i)
                for(int j=0; j<9; ++j)
                    this.addSlot(new LedgerSlot(container,j+i*9,8+j*18,18+i*18));
            for(int i=0; i<3; ++i)
                for(int j=0; j<9; ++j)
                    this.addSlot(new Slot(inventory,j+i*9+9,8+j*18,85+i*18));
            for(int i=0; i<9; ++i)
                this.addSlot(new Slot(inventory,i,8+i*18,143));
        }

        @Override
        public boolean stillValid(@NotNull Player player) {
            return this.container.stillValid(player);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
            ItemStack ret = ItemStack.EMPTY;
            Slot slot = this.slots.get(index);
            if(Objects.nonNull(slot) && slot.hasItem()) {
                ItemStack stack = slot.getItem();
                ret = stack.copy();
                if((index<27 && !this.moveItemStackTo(stack,27,this.slots.size(),true)) ||
                        !this.moveItemStackTo(stack,0,27,false))
                    return ItemStack.EMPTY;
                if(stack.isEmpty()) slot.set(ItemStack.EMPTY);
                else slot.setChanged();
            }
            return ret;
        }
    }

    private static final class LedgerSlot extends Slot {

        private LedgerSlot(Container container, int index, int x, int y) {
            super(container,index,x,y);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return this.getItem().isEmpty();
        }

        @Override
        public boolean mayPickup(@NotNull Player player) {
            return false;
        }
    }
}
