package mods.thecomputerizer.reputation.registry;

import mods.thecomputerizer.reputation.ReputationRef;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static mods.thecomputerizer.reputation.ReputationRef.MODID;
import static net.minecraftforge.registries.ForgeRegistries.Keys.SOUND_EVENTS;

public class SoundRegistry {
    
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(SOUND_EVENTS,MODID);
    
    public static final RegistryObject<SoundEvent> INCREASE_REPUTATION = make("increase_reputation");
    public static final RegistryObject<SoundEvent> DECREASE_REPUTATION = make("decrease_reputation");
    public static final RegistryObject<SoundEvent> FLEE = make("flee");
    public static final RegistryObject<SoundEvent> LEDGER_PLACE = make("ledger_place");
    public static final RegistryObject<SoundEvent> LEDGER_SIGN = make("ledger_sign");
    
    private static RegistryObject<SoundEvent> make(String name) {
        return SOUNDS.register(name,() -> new SoundEvent(ReputationRef.res(name)));
    }

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}