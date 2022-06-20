package mods.thecomputerizer.reputation.common.registration;

import mods.thecomputerizer.reputation.common.ModDefinitions;
import mods.thecomputerizer.reputation.common.objects.items.FactionCurrencyBag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Items {
    public static DeferredRegister<Item> ITEM_REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ModDefinitions.MODID);
    public static RegistryObject<Item> LEDGER_ITEM = ITEM_REGISTRY.register("ledger",() -> new BlockItem(Blocks.LEDGER.get(), new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_MISC).rarity(Rarity.RARE)));
    public static RegistryObject<Item> LEDGER_BOOK_ITEM = ITEM_REGISTRY.register("ledger_book",() -> new BlockItem(Blocks.LEDGER_BOOK.get(), new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_MISC).rarity(Rarity.RARE)));
    public static RegistryObject<Item> FACTION_BAG = ITEM_REGISTRY.register("faction_bag",() -> new FactionCurrencyBag(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_MISC).rarity(Rarity.EPIC)));

    public static void register(IEventBus bus) {
        ITEM_REGISTRY.register(bus);
    }
}
