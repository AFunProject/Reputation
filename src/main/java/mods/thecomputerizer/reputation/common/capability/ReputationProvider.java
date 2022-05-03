package mods.thecomputerizer.reputation.common.capability;

import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.api.capability.IReputation;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public class ReputationProvider implements ICapabilitySerializable<CompoundTag> {

	private final IReputation impl;

	public ReputationProvider() {
		impl = new ReputationCapability();
	}

	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
		return cap == ReputationHandler.REPUTATION_CAPABILITY ? LazyOptional.of(() -> impl).cast() : LazyOptional.empty();
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