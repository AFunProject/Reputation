package mods.thecomputerizer.reputation.registry;

import mods.thecomputerizer.reputation.registry.blockentities.LedgerBookEntity;
import mods.thecomputerizer.reputation.registry.blockentities.LedgerEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static mods.thecomputerizer.reputation.ReputationRef.MODID;
import static mods.thecomputerizer.reputation.registry.BlockRegistry.LEDGER;
import static mods.thecomputerizer.reputation.registry.BlockRegistry.LEDGER_BOOK;
import static net.minecraftforge.registries.ForgeRegistries.BLOCK_ENTITIES;

public class BlockEntitiesRegistry {
    
    public static DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_REGISTRY =
            DeferredRegister.create(BLOCK_ENTITIES,MODID);
    
    public static RegistryObject<BlockEntityType<LedgerEntity>> LEDGER_ENTITY =
            make("ledger_entity",() -> LedgerEntity::new,() -> LEDGER);
    
    public static RegistryObject<BlockEntityType<LedgerBookEntity>> LEDGER_BOOK_ENTITY =
            make("ledger_book_entity",() -> LedgerBookEntity::new,() -> LEDGER_BOOK);
    
    @SuppressWarnings("ConstantConditions")
    private static <T extends BlockEntity> Supplier<BlockEntityType<T>> builder(
            Supplier<BlockEntitySupplier<T>> supplier, Supplier<RegistryObject<? extends Block>> registeredBlock) {
        return () -> Builder.of(supplier.get(),registeredBlock.get().get()).build(null);
    }
    
    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> make(String name,
            Supplier<BlockEntitySupplier<T>> supplier, Supplier<RegistryObject<? extends Block>> registeredBlock) {
        return BLOCK_ENTITY_REGISTRY.register(name,builder(supplier,registeredBlock));
    }

    public static void register(IEventBus bus) {
        BLOCK_ENTITY_REGISTRY.register(bus);
    }
}