package mods.thecomputerizer.reputation.registry;

import mods.thecomputerizer.reputation.Constants;
import mods.thecomputerizer.reputation.registry.items.FactionCurrencyBag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("unused")
public class ItemRegistry {
    public static DeferredRegister<Item> ITEM_REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MODID);
    public static RegistryObject<Item> LEDGER_ITEM = ITEM_REGISTRY.register("ledger",
            () -> new BlockItem(BlockRegistry.LEDGER.get(), new Item.Properties().stacksTo(1)
                    .tab(RegistryHandler.REPUTATION_TAB).rarity(Rarity.RARE)));
    public static RegistryObject<Item> LEDGER_BOOK_ITEM = ITEM_REGISTRY.register("ledger_book",
            () -> new BlockItem(BlockRegistry.LEDGER_BOOK.get(), new Item.Properties().stacksTo(1)
                    .tab(RegistryHandler.REPUTATION_TAB).rarity(Rarity.RARE)));
    public static RegistryObject<Item> FACTION_BAG = ITEM_REGISTRY.register("faction_bag",
            () -> new FactionCurrencyBag(new Item.Properties().stacksTo(1)
                    .tab(RegistryHandler.REPUTATION_TAB).rarity(Rarity.EPIC)));

    public static void register(IEventBus bus) {
        ITEM_REGISTRY.register(bus);
    }
}
