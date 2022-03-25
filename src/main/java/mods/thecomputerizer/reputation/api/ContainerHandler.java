package mods.thecomputerizer.reputation.api;

import mods.thecomputerizer.reputation.api.capability.IPlacedContainer;
import mods.thecomputerizer.reputation.common.capability.PlacedContainerProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;

public class ContainerHandler {

    public static boolean changesReputation(BlockEntity entity) {
        LazyOptional<IPlacedContainer> optional = entity.getCapability(PlacedContainerProvider.PLACED_CONTAINER_CAPABILITY);
        if (optional.resolve().isPresent()) {
            IPlacedContainer container = optional.resolve().get();
            return container.getCheck();
        }
        return true;
    }

    public static void setChangesReputation(BlockEntity entity, boolean check) {
        LazyOptional<IPlacedContainer> optional = entity.getCapability(PlacedContainerProvider.PLACED_CONTAINER_CAPABILITY);
        if (optional.resolve().isPresent()) {
            IPlacedContainer container = optional.resolve().get();
            container.setCheck(check);
        }
    }
}
