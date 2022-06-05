package mods.thecomputerizer.reputation.common.registration;

import mods.thecomputerizer.reputation.common.ModDefinitions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Sounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ModDefinitions.MODID);

    public static final RegistryObject<SoundEvent> INCREASE_REPUTATION = SOUNDS.register("increase_reputation", () -> new SoundEvent(new ResourceLocation(ModDefinitions.MODID,"increase_reputation")));
    public static final RegistryObject<SoundEvent> DECREASE_REPUTATION = SOUNDS.register("decrease_reputation", () -> new SoundEvent(new ResourceLocation(ModDefinitions.MODID,"decrease_reputation")));
    public static final RegistryObject<SoundEvent> FLEE = SOUNDS.register("flee", () -> new SoundEvent(new ResourceLocation(ModDefinitions.MODID,"flee")));

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}
