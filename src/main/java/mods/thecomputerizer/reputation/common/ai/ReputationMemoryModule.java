package mods.thecomputerizer.reputation.common.ai;

import com.mojang.serialization.Codec;
import mods.thecomputerizer.reputation.Constants;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ReputationMemoryModule<U> extends MemoryModuleType<U> {

    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULES =
            DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, Constants.MODID);
    public static final RegistryObject<MemoryModuleType<Player>> NEAREST_PLAYER_BAD_REPUTATION =
            MEMORY_MODULES.register("nearest_player_bad_reputation",
                    () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<Player>> NEAREST_PLAYER_NEUTRAL_REPUTATION =
            MEMORY_MODULES.register("nearest_player_neutral_reputation",
                    () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<Player>> NEAREST_PLAYER_GOOD_REPUTATION =
            MEMORY_MODULES.register("nearest_player_good_reputation",
                    () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<Player>> FLEE_FROM_PLAYER =
            MEMORY_MODULES.register("flee_from_player",
                    () -> new MemoryModuleType<>(Optional.empty()));

    public ReputationMemoryModule(Optional<Codec<U>> codec) {
        super(codec);
    }

    public static MemoryModuleType<Player> getNearestModuleFromString(String bound) {
        if(bound.matches("good")) return NEAREST_PLAYER_GOOD_REPUTATION.get();
        else if(bound.matches("neutral")) return NEAREST_PLAYER_NEUTRAL_REPUTATION.get();
        return NEAREST_PLAYER_BAD_REPUTATION.get();
    }
}
