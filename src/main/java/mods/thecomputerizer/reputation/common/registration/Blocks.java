package mods.thecomputerizer.reputation.common.registration;

import mods.thecomputerizer.reputation.common.ModDefinitions;
import mods.thecomputerizer.reputation.common.objects.blocks.Ledger;
import mods.thecomputerizer.reputation.common.objects.blocks.LedgerBook;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Blocks {
    public static DeferredRegister<Block> BLOCK_REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, ModDefinitions.MODID);
    public static RegistryObject<Block> LEDGER = BLOCK_REGISTRY.register("ledger",() -> new Ledger(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD).randomTicks()));
    public static RegistryObject<Block> LEDGER_BOOK = BLOCK_REGISTRY.register("ledger_book",() -> new LedgerBook(BlockBehaviour.Properties.of(Material.WOOD).strength(2F).sound(SoundType.WOOD).randomTicks()));

    public static void register(IEventBus bus) {
        BLOCK_REGISTRY.register(bus);
    }
}
