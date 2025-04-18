package mods.thecomputerizer.reputation.capability.handlers;

import mods.thecomputerizer.reputation.capability.placedcontainer.IPlacedContainer;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.Objects;

import static mods.thecomputerizer.reputation.capability.placedcontainer.PlacedContainerProvider.PLACED_CONTAINER_CAPABILITY;

public class ContainerHandler {

    @SuppressWarnings("DataFlowIssue")
    private static @Nullable IPlacedContainer getCapability(BlockEntity entity) {
        return entity.getCapability(PLACED_CONTAINER_CAPABILITY).orElse(null);
    }

    public static boolean changesReputation(BlockEntity entity) {
        IPlacedContainer cap = getCapability(entity);
        if(Objects.nonNull(cap)) return cap.getCheck();
        return true;
    }

    public static void setChangesReputation(BlockEntity entity, boolean check) {
        IPlacedContainer cap = getCapability(entity);
        if(Objects.nonNull(cap)) cap.setCheck(check);
    }
}