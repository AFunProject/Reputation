package mods.thecomputerizer.reputation.common.capability;

import mods.thecomputerizer.reputation.api.capability.IPlacedContainer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public class PlacedContainerProvider implements ICapabilitySerializable<CompoundTag> {

    public static Capability<IPlacedContainer> PLACED_CONTAINER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private final IPlacedContainer impl;

    public PlacedContainerProvider() {
        impl = new PlacedContainer();
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        return cap == PLACED_CONTAINER_CAPABILITY ? LazyOptional.of(() -> impl).cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return impl.writeNBT(new CompoundTag());
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        impl.readNBT(nbt);
    }
}
