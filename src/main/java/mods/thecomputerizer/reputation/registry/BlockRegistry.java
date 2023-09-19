package mods.thecomputerizer.reputation.registry;

import mods.thecomputerizer.reputation.Constants;
import mods.thecomputerizer.reputation.registry.blocks.Ledger;
import mods.thecomputerizer.reputation.registry.blocks.LedgerBook;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockRegistry {
    public static DeferredRegister<Block> BLOCK_REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS,Constants.MODID);
    public static RegistryObject<Block> LEDGER = BLOCK_REGISTRY.register("ledger",() ->
            new Ledger(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5f).sound(SoundType.WOOD)));
    public static RegistryObject<Block> LEDGER_BOOK = BLOCK_REGISTRY.register("ledger_book",() ->
            new LedgerBook(BlockBehaviour.Properties.of(Material.WOOD).strength(2f).sound(SoundType.WOOD)));

    public static void register(IEventBus bus) {
        BLOCK_REGISTRY.register(bus);
    }
}
