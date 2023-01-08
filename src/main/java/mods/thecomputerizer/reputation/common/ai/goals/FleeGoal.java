package mods.thecomputerizer.reputation.common.ai.goals;

import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.PlayerFactionHandler;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.client.event.RenderEvents;
import mods.thecomputerizer.reputation.common.network.FleeIconMessage;
import mods.thecomputerizer.reputation.common.network.PacketHandler;
import net.minecraft.server.level.ServerPlayer;
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
import java.util.Random;

public class FleeGoal extends Goal {
    @Nullable
    protected Path path;
    protected final PathNavigation pathNav;
    private final Mob mob;
    private Player player;
    private final double speed;
    private final boolean checkReputation;
    private boolean startFlee;
    private final Random random;

    public FleeGoal(Mob mob, double speed, boolean checkReputation) {
        this.mob = mob;
        this.player = null;
        this.speed = speed;
        this.checkReputation = checkReputation;
        this.startFlee = false;
        this.random = new Random();
        this.pathNav = mob.getNavigation();
    }

    @Override
    public boolean canUse() {
        if(this.checkReputation) return checkWithReputation();
        else return checkWithPlayerReputation();
    }

    private boolean checkWithReputation() {
        Level level = this.mob.level;
        List<? extends Player> list = level.players().stream().filter(EntitySelector.NO_SPECTATORS)
                .filter((p) -> this.mob.closerThan(p, 16.0D))
                .filter((p) -> p.getCapability(ReputationHandler.REPUTATION_CAPABILITY).isPresent())
                .sorted(Comparator.comparingDouble(this.mob::distanceToSqr)).toList();
        if(!list.isEmpty()) {
            Player nearest = list.get(0);
            for (Faction f : ReputationHandler.getEntityFactions(this.mob)) {
                int reputation = ReputationHandler.getReputation(nearest, f);
                if(reputation<=f.getLowerRep()) {
                    this.player = nearest;
                    Vec3 vec3 = DefaultRandomPos.getPosAway((PathfinderMob) this.mob, 16, 7, this.player.position());
                    if (vec3 == null) return false;
                    else if (this.player.distanceToSqr(vec3.x, vec3.y, vec3.z) < this.player.distanceToSqr(this.mob)) return false;
                    else {
                        this.pathNav.setSpeedModifier(this.speed);
                        this.path = this.pathNav.createPath(vec3.x, vec3.y, vec3.z, 0);
                        if (this.path != null) {
                            this.startFlee = true;
                            return true;
                        }
                    }
                }
                else {
                    this.startFlee = false;
                    return false;
                }
            }
        }
        this.startFlee = false;
        return false;
    }

    private boolean checkWithPlayerReputation() {
        float percent = this.mob.getHealth()/this.mob.getMaxHealth();
        if(this.player!=null || this.mob.getLastHurtByMob() instanceof Player ) {
            if(this.mob.getLastHurtByMob() instanceof Player p) this.player = p;
            if (this.mob.distanceTo(this.player)<=28 && percent <= .5f) {
                boolean inFaction = ReputationHandler.getEntityFactions(this.mob).isEmpty();
                for (Faction f : ReputationHandler.getEntityFactions(this.mob)) {
                    if (PlayerFactionHandler.isPlayerInFaction(this.player, f)) inFaction = true;
                }
                if (!inFaction && this.random.nextFloat(51f)>=0f && !this.startFlee) {
                    this.startFlee = true;
                    if (this.player instanceof ServerPlayer)
                        PacketHandler.sendTo(new FleeIconMessage(this.mob.getUUID(), true), (ServerPlayer) this.player);
                    else if (!RenderEvents.fleeingMobs.contains(this.mob.getUUID()))
                        RenderEvents.fleeingMobs.add(this.mob.getUUID());
                }
                if(this.startFlee) {
                    Vec3 vec3 = DefaultRandomPos.getPosAway((PathfinderMob) this.mob, 32, 7, this.player.position());
                    if (vec3 == null) return false;
                    else if (this.player.distanceToSqr(vec3.x, vec3.y, vec3.z) < this.player.distanceToSqr(this.mob)) return false;
                    else {
                        this.pathNav.setSpeedModifier(this.speed);
                        this.path = this.pathNav.createPath(vec3.x, vec3.y, vec3.z, 0);
                        return this.path != null;
                    }
                }
            }
            else if(this.startFlee) {
                this.startFlee = false;
                if (this.player instanceof ServerPlayer)
                    PacketHandler.sendTo(new FleeIconMessage(this.mob.getUUID(), false), (ServerPlayer) this.player);
                else RenderEvents.fleeingMobs.remove(this.mob.getUUID());
            }
        }
        else if(this.startFlee) {
            this.startFlee = false;
            if (this.player!=null && this.player instanceof ServerPlayer)
                PacketHandler.sendTo(new FleeIconMessage(this.mob.getUUID(), false), (ServerPlayer) this.player);
            else RenderEvents.fleeingMobs.remove(this.mob.getUUID());
        }
        if (percent <= .5f && startFlee && !this.mob.isDeadOrDying() && this.mob.distanceTo(this.player)>28) {
            for (Faction f : ReputationHandler.getEntityFactions(this.mob)) {
                ReputationHandler.changeReputation(this.player, f, -1 * f.getActionWeighting("fleeing"));
            }
            this.mob.discard();
        }
        return this.startFlee;
    }

    @Override
    public boolean canContinueToUse() {
        if(this.pathNav.isDone() && this.startFlee) {
            Vec3 vec3 = DefaultRandomPos.getPosAway((PathfinderMob) this.mob, 32, 7, this.player.position());
            if (!(vec3 == null) && !(this.player.distanceToSqr(vec3.x, vec3.y, vec3.z) < this.player.distanceToSqr(this.mob))) {
                this.path = this.pathNav.createPath(vec3.x, vec3.y, vec3.z, 0);
                this.pathNav.moveTo(this.path, this.speed);
            }
        }
        return this.startFlee;
    }

    @Override
    public void start() {
        this.pathNav.moveTo(this.path, this.speed);
    }

    @Override
    public void stop() {
        this.player = null;
    }

    @Override
    public void tick() {
        this.pathNav.setSpeedModifier(this.speed);
        this.mob.getNavigation().setSpeedModifier(this.speed);
    }
}
