package mods.thecomputerizer.reputation.common.ai.goals;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import java.util.EnumSet;

import static net.minecraft.world.entity.ai.goal.Goal.Flag.TARGET;

public class FleeBattleTargetOverride extends TargetGoal {

    private final FleeBattleGoal parentGoal;
    private final PathfinderMob mob;

    public FleeBattleTargetOverride(FleeBattleGoal parent, PathfinderMob mob) {
        super(mob, false);
        this.parentGoal = parent;
        this.mob = mob;
        this.setFlags(EnumSet.of(TARGET));
    }

    @Override public boolean canUse() {
        return parentGoal.isFleeing;
    }

    @Override public boolean canContinueToUse() {
        return parentGoal.isFleeing;
    }

    @Override public void start() {
        this.mob.setTarget(null);
    }
}