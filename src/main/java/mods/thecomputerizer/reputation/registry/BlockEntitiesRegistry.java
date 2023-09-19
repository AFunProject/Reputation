package mods.thecomputerizer.reputation.registry;

import mods.thecomputerizer.reputation.Constants;
import mods.thecomputerizer.reputation.registry.blockentities.LedgerBookEntity;
import mods.thecomputerizer.reputation.registry.blockentities.LedgerEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("ConstantConditions")
public class BlockEntitiesRegistry {
    public static DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_REGISTRY =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Constants.MODID);
    public static RegistryObject<BlockEntityType<LedgerEntity>> LEDGER_ENTITY = BLOCK_ENTITY_REGISTRY.register(
            "ledger_entity", () -> BlockEntityType.Builder.of(LedgerEntity::new,BlockRegistry.LEDGER.get())
                    .build(null));
    public static RegistryObject<BlockEntityType<LedgerBookEntity>> LEDGER_BOOK_ENTITY = BLOCK_ENTITY_REGISTRY.register(
            "ledger_book_entity", () -> BlockEntityType.Builder.of(LedgerBookEntity::new,BlockRegistry.LEDGER_BOOK.get())
                    .build(null));


    public static void register(IEventBus bus) {
        BLOCK_ENTITY_REGISTRY.register(bus);
    }
}
