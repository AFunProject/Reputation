package mods.thecomputerizer.reputation.registry;

import mods.thecomputerizer.reputation.registry.items.FactionCurrencyBag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;
import java.util.function.Supplier;

import static mods.thecomputerizer.reputation.ReputationRef.MODID;
import static mods.thecomputerizer.reputation.registry.BlockRegistry.LEDGER;
import static mods.thecomputerizer.reputation.registry.BlockRegistry.LEDGER_BOOK;
import static mods.thecomputerizer.reputation.registry.RegistryHandler.REPUTATION_TAB;
import static net.minecraft.world.item.Rarity.EPIC;
import static net.minecraft.world.item.Rarity.RARE;
import static net.minecraftforge.registries.ForgeRegistries.ITEMS;

@SuppressWarnings("unused")
public class ItemRegistry {
    
    public static DeferredRegister<Item> ITEM_REGISTRY = DeferredRegister.create(ITEMS,MODID);
    
    public static RegistryObject<BlockItem> LEDGER_ITEM = makeBlock("ledger",() -> LEDGER);
    public static RegistryObject<BlockItem> LEDGER_BOOK_ITEM = makeBlock("ledger_book",() -> LEDGER_BOOK);
    
    public static RegistryObject<FactionCurrencyBag> FACTION_BAG =
            make("faction_bag",builder(FactionCurrencyBag::new,EPIC));
    
    private static Supplier<BlockItem> blockItemBuilder(Supplier<RegistryObject<? extends Block>> block) {
        return builder(properties -> new BlockItem(block.get().get(),properties),RARE);
    }
    
    private static <I extends Item> Supplier<I> builder(Function<Properties,I> itemSupplier, Rarity rarity) {
        return () -> itemSupplier.apply(new Properties().stacksTo(1).tab(REPUTATION_TAB).rarity(rarity));
    }
    
    private static <I extends Item> RegistryObject<I> make(String name, Supplier<I> itemSupplier) {
        return ITEM_REGISTRY.register(name,itemSupplier);
    }
    
    private static RegistryObject<BlockItem> makeBlock(String name, Supplier<RegistryObject<? extends Block>> block) {
        return make(name,blockItemBuilder(block));
    }

    public static void register(IEventBus bus) {
        ITEM_REGISTRY.register(bus);
    }
}