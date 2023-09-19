package mods.thecomputerizer.reputation.capability.playerfaction;

import mods.thecomputerizer.reputation.capability.Faction;
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

    private final IPlayerFaction instance;

    public PlayerFactionProvider(Faction faction) {
        instance = new PlayerFaction(faction);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        return cap == PLAYER_FACTION ? LazyOptional.of(() -> instance).cast() : LazyOptional.empty();
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
