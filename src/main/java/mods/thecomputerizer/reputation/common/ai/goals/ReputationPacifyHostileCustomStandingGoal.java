package mods.thecomputerizer.reputation.common.ai.goals;

import mods.thecomputerizer.reputation.util.HelperMethods;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

public class ReputationPacifyHostileCustomStandingGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    public final Mob self;
    private final String standing;

    public ReputationPacifyHostileCustomStandingGoal(Mob mob, Class<T> tclass, boolean mustSee, boolean mustReach, String standing) {
        super(mob,tclass,mustSee,mustReach);
        this.self = mob;
        this.standing = standing;
    }

    @Override
    public boolean canUse() {
        if (HelperMethods.getNearestPlayerInCustomStandingToEntity(this.self,16d,this.standing)!=null) return false;
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (HelperMethods.getNearestPlayerInCustomStandingToEntity(this.self,16d,this.standing)!=null) return false;
        return super.canContinueToUse();
    }
}
