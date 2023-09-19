package mods.thecomputerizer.reputation.common.ai;

import mods.thecomputerizer.reputation.Constants;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ReputationSenorType<U extends Sensor<?>> extends SensorType<U> {
    public static final DeferredRegister<SensorType<?>> SENSOR_TYPES =
            DeferredRegister.create(ForgeRegistries.SENSOR_TYPES, Constants.MODID);
    public static final RegistryObject<SensorType<ReputationSensor>> NEAREST_PLAYER_REPUTATION =
            SENSOR_TYPES.register("nearest_player_reputation", () -> new SensorType<>(ReputationSensor::new));

    public ReputationSenorType(Supplier<U> sup) {
        super(sup);
    }
}
