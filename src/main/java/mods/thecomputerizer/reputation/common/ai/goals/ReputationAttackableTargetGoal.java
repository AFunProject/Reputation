package mods.thecomputerizer.reputation.common.ai.goals;

import mods.thecomputerizer.reputation.util.HelperMethods;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

import java.util.Objects;

public class ReputationAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {

    public Mob self;

    public ReputationAttackableTargetGoal(Mob mob, Class<T> tclass, boolean mustSee, boolean mustReach) {
        super(mob,tclass,mustSee,mustReach);
        this.self = mob;
    }

    @Override
    public boolean canUse() {
        return Objects.nonNull(HelperMethods.getNearestPlayerInBadStandingToEntity(this.self,16d)) && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return Objects.nonNull(HelperMethods.getNearestPlayerInBadStandingToEntity(this.self,16d)) && super.canContinueToUse();
    }
}
