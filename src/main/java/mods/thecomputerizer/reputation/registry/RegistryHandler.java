package mods.thecomputerizer.reputation.registry;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;

import javax.annotation.Nonnull;

import static mods.thecomputerizer.reputation.ReputationRef.MODID;
import static mods.thecomputerizer.reputation.common.ai.ReputationMemoryModule.MEMORY_MODULES;
import static mods.thecomputerizer.reputation.common.ai.ReputationSenorType.SENSOR_TYPES;
import static mods.thecomputerizer.reputation.registry.ItemRegistry.FACTION_BAG;
import static net.minecraftforge.api.distmarker.Dist.CLIENT;

@MethodsReturnNonnullByDefault
public class RegistryHandler {

    public static final CreativeModeTab REPUTATION_TAB = new CreativeModeTab(MODID) {
        @OnlyIn(CLIENT)
        public @Nonnull ItemStack makeIcon() {
            return new ItemStack(FACTION_BAG.get());
        }
    };

    public static void initRegistries(IEventBus bus) {
        BlockRegistry.register(bus);
        ItemRegistry.register(bus);
        RecipeRegistry.register(bus);
        BlockEntitiesRegistry.register(bus);
        SoundRegistry.register(bus);
        MEMORY_MODULES.register(bus);
        SENSOR_TYPES.register(bus);
    }
}