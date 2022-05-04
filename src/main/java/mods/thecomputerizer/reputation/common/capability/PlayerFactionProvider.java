package mods.thecomputerizer.reputation.common.capability;

import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.capability.IPlayerFaction;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public class PlayerFactionProvider implements ICapabilitySerializable<CompoundTag> {

    public Capability<IPlayerFaction> PLAYER_FACTION = CapabilityManager.get(new CapabilityToken<>() {});

    private final IPlayerFaction impl;

    public PlayerFactionProvider(Faction faction) {
        impl = new PlayerFaction(faction);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        return cap == PLAYER_FACTION ? LazyOptional.of(() -> impl).cast() : LazyOptional.empty();
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
