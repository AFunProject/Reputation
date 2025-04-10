package mods.thecomputerizer.reputation.registry;

import mods.thecomputerizer.reputation.ReputationRef;
import mods.thecomputerizer.reputation.common.ai.ReputationMemoryModule;
import mods.thecomputerizer.reputation.common.ai.ReputationSenorType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;

import javax.annotation.Nonnull;

public class RegistryHandler {

    public static final CreativeModeTab REPUTATION_TAB = new CreativeModeTab(ReputationRef.MODID) {
        @OnlyIn(Dist.CLIENT)
        public @Nonnull ItemStack makeIcon() {
            return new ItemStack(ItemRegistry.FACTION_BAG.get());
        }
    };

    public static void initRegistries(IEventBus bus) {
        BlockRegistry.register(bus);
        ItemRegistry.register(bus);
        RecipeRegistry.register(bus);
        BlockEntitiesRegistry.register(bus);
        SoundRegistry.register(bus);
        ReputationMemoryModule.MEMORY_MODULES.register(bus);
        ReputationSenorType.SENSOR_TYPES.register(bus);
    }

    public static void queuePackets() {
    }
}
