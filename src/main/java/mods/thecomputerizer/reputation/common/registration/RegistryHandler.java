package mods.thecomputerizer.reputation.common.registration;

import mods.thecomputerizer.reputation.common.ai.ReputationMemoryModule;
import mods.thecomputerizer.reputation.common.ai.ReputationSenorType;
import net.minecraftforge.eventbus.api.IEventBus;

public class RegistryHandler {

    public static void registerCommonObjects(IEventBus bus) {
        Blocks.register(bus);
        Items.register(bus);
        Recipes.register(bus);
        Entities.register(bus);
        Sounds.register(bus);
        ReputationMemoryModule.MEMORY_MODULES.register(bus);
        ReputationSenorType.SENSOR_TYPES.register(bus);
    }
}
