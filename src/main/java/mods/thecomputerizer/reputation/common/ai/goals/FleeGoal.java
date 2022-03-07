package mods.thecomputerizer.reputation.common.ai.goals;

import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FleeGoal extends Goal {
    @Nullable
    protected Path path;
    protected final PathNavigation pathNav;
    private final Mob mob;
    private Player player;
    private double speed;

    public FleeGoal(Mob mob, double speed) {
        this.mob = mob;
        this.player = null;
        this.speed = speed;
        this.pathNav = mob.getNavigation();
    }

    @Override
    public boolean canUse() {
        Level level = this.mob.level;
        List<Player> list = level.players().stream().filter(EntitySelector.NO_SPECTATORS)
                .filter((p) -> this.mob.closerThan(p, 16.0D))
                .filter((p) -> p.getCapability(ReputationHandler.REPUTATION_CAPABILITY).isPresent())
                .sorted(Comparator.comparingDouble(this.mob::distanceToSqr))
                .collect(Collectors.toList());
        if(!list.isEmpty()) {
            Player nearest = list.get(0);
            for (Faction f : ReputationHandler.getEntityFactions(this.mob)) {
                int reputation = ReputationHandler.getReputation(nearest, f);
                if(reputation<=-50) {
                    this.player = nearest;
                    Vec3 vec3 = DefaultRandomPos.getPosAway((PathfinderMob) this.mob, 16, 7, this.player.position());
                    if (vec3 == null) {
                        return false;
                    } else if (this.player.distanceToSqr(vec3.x, vec3.y, vec3.z) < this.player.distanceToSqr(this.mob)) {
                        return false;
                    } else {
                        this.path = this.pathNav.createPath(vec3.x, vec3.y, vec3.z, 0);
                        return this.path != null;
                    }
                }
                else {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.pathNav.isDone();
    }

    @Override
    public void start() {
        this.pathNav.moveTo(this.path, 1d);
    }

    @Override
    public void stop() {
        this.player = null;
    }

    @Override
    public void tick() {
        if (this.mob.distanceToSqr(this.player) < 32d) {
            this.mob.getNavigation().setSpeedModifier(this.speed);
        } else {
            this.mob.getNavigation().setSpeedModifier(1d);
        }
    }
}
