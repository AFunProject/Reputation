package mods.thecomputerizer.reputation.capability.reputation;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public class ReputationProvider implements ICapabilitySerializable<CompoundTag> {

	public static final Capability<IReputation> REPUTATION_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
	private final IReputation instance;

	public ReputationProvider() {
		instance = new ReputationCapability();
	}

	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
		return cap == REPUTATION_CAPABILITY ? LazyOptional.of(() -> instance).cast() : LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT() {
		return instance.writeTag(new CompoundTag());
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		instance.readTag(nbt);
	}

}