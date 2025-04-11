package mods.thecomputerizer.reputation.registry;

import mods.thecomputerizer.reputation.registry.blocks.Ledger;
import mods.thecomputerizer.reputation.registry.blocks.LedgerBook;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;
import java.util.function.Supplier;

import static mods.thecomputerizer.reputation.ReputationRef.MODID;
import static net.minecraft.world.level.material.Material.WOOD;
import static net.minecraftforge.registries.ForgeRegistries.BLOCKS;

public class BlockRegistry {
    
    public static DeferredRegister<Block> BLOCK_REGISTRY = DeferredRegister.create(BLOCKS,MODID);
    
    public static RegistryObject<Ledger> LEDGER = make("ledger",Ledger::new,2.5f);
    public static RegistryObject<LedgerBook> LEDGER_BOOK = make("ledger_book",LedgerBook::new,2f);
    
    private static <B extends Block> Supplier<B> builder(Function<Properties,B> blockSupplier, float strength) {
        return () -> blockSupplier.apply(Properties.of(WOOD).strength(strength).sound(SoundType.WOOD));
    }
    
    private static <B extends Block> RegistryObject<B> make(String name, Function<Properties,B> blockSupplier,
            float strength) {
        return BLOCK_REGISTRY.register(name,builder(blockSupplier,strength));
    }

    public static void register(IEventBus bus) {
        BLOCK_REGISTRY.register(bus);
    }
}