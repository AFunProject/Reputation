package mods.thecomputerizer.reputation.common.ai;

import com.google.common.collect.ImmutableSet;
import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class ReputationSensor extends Sensor<LivingEntity> {
    public @NotNull Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(ReputationMemoryModule.NEAREST_PLAYER_BAD_REPUTATION.get(), ReputationMemoryModule.NEAREST_PLAYER_NEUTRAL_REPUTATION.get(), ReputationMemoryModule.NEAREST_PLAYER_GOOD_REPUTATION.get());
    }

    protected void doTick(ServerLevel level, LivingEntity entity) {
        List<Player> list = level.players().stream().filter(EntitySelector.NO_SPECTATORS)
                .filter((p) -> entity.closerThan(p, 16.0D))
                .filter((p) -> isEntityTargetable(entity, p))
                .filter((p) -> p.getCapability(ReputationHandler.REPUTATION_CAPABILITY).isPresent())
                .sorted(Comparator.comparingDouble(entity::distanceToSqr))
                .collect(Collectors.toList());
        if(!list.isEmpty()) {
            Player nearest = list.get(0);
            for (Faction f : ReputationHandler.getEntityFactions(entity)) {
                int reputation = ReputationHandler.getReputation(nearest, f);
                if(reputation<=f.getLowerRep()) {
                    entity.getBrain().setMemory(ReputationMemoryModule.NEAREST_PLAYER_BAD_REPUTATION.get(), nearest);
                    entity.getBrain().setMemory(ReputationMemoryModule.NEAREST_PLAYER_NEUTRAL_REPUTATION.get(), Optional.empty());
                    entity.getBrain().setMemory(ReputationMemoryModule.NEAREST_PLAYER_GOOD_REPUTATION.get(), Optional.empty());
                }
                else if(reputation<f.getHigherRep()) {
                    entity.getBrain().setMemory(ReputationMemoryModule.NEAREST_PLAYER_NEUTRAL_REPUTATION.get(), nearest);
                    entity.getBrain().setMemory(ReputationMemoryModule.NEAREST_PLAYER_BAD_REPUTATION.get(), Optional.empty());
                    entity.getBrain().setMemory(ReputationMemoryModule.NEAREST_PLAYER_GOOD_REPUTATION.get(), Optional.empty());
                }
                else {
                    entity.getBrain().setMemory(ReputationMemoryModule.NEAREST_PLAYER_GOOD_REPUTATION.get(), nearest);
                    entity.getBrain().setMemory(ReputationMemoryModule.NEAREST_PLAYER_BAD_REPUTATION.get(), Optional.empty());
                    entity.getBrain().setMemory(ReputationMemoryModule.NEAREST_PLAYER_NEUTRAL_REPUTATION.get(), Optional.empty());
                }
            }
        }
    }
}
