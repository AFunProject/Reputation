package mods.thecomputerizer.reputation.registry;

import mods.thecomputerizer.reputation.Constants;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundRegistry {
    public static final DeferredRegister<SoundEvent> SOUNDS = 
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,Constants.MODID);
    public static final RegistryObject<SoundEvent> INCREASE_REPUTATION = SOUNDS.register("increase_reputation", 
            () -> new SoundEvent(Constants.res("increase_reputation")));
    public static final RegistryObject<SoundEvent> DECREASE_REPUTATION = SOUNDS.register("decrease_reputation", 
            () -> new SoundEvent(Constants.res("decrease_reputation")));
    public static final RegistryObject<SoundEvent> FLEE = SOUNDS.register("flee", 
            () -> new SoundEvent(Constants.res("flee")));
    public static final RegistryObject<SoundEvent> LEDGER_PLACE = SOUNDS.register("ledger_place", 
            () -> new SoundEvent(Constants.res("ledger_place")));
    public static final RegistryObject<SoundEvent> LEDGER_SIGN = SOUNDS.register("ledger_sign", 
            () -> new SoundEvent(Constants.res("ledger_sign")));

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}
