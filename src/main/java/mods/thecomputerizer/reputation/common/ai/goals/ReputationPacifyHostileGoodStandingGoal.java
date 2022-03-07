package mods.thecomputerizer.reputation.common.ai.goals;

import mods.thecomputerizer.reputation.util.HelperMethods;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

public class ReputationPacifyHostileGoodStandingGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    public Mob self;

    public ReputationPacifyHostileGoodStandingGoal(Mob mob, Class<T> tclass, boolean mustSee, boolean mustReach) {
        super(mob,tclass,mustSee,mustReach);
        this.self = mob;
    }

    @Override
    public boolean canUse() {
        if (HelperMethods.getNearestPlayerInGoodStandingToEntity(this.self,16d)!=null) return false;
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (HelperMethods.getNearestPlayerInGoodStandingToEntity(this.self,16d)!=null) return false;
        return super.canContinueToUse();
    }
}
