package mods.thecomputerizer.reputation.common.ai;

import com.google.common.collect.ImmutableSet;
import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.PlayerFactionHandler;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.client.event.RenderEvents;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import mods.thecomputerizer.reputation.common.event.WorldEvents;
import mods.thecomputerizer.reputation.common.network.FleeIconMessage;
import mods.thecomputerizer.reputation.common.network.PacketHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ReputationSensor extends Sensor<LivingEntity> {
    
    private boolean startFlee = false;
    private final Random random = new Random();
    private Player player = null;


    @Override
    public @NotNull Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(ReputationMemoryModule.NEAREST_PLAYER_BAD_REPUTATION.get(), ReputationMemoryModule.NEAREST_PLAYER_NEUTRAL_REPUTATION.get(), ReputationMemoryModule.NEAREST_PLAYER_GOOD_REPUTATION.get(), ReputationMemoryModule.FLEE_FROM_PLAYER.get());
    }

    @Override
    protected void doTick(ServerLevel level, LivingEntity entity) {
        List<ServerPlayer> list = level.players().stream().filter(EntitySelector.NO_SPECTATORS)
                .filter((p) -> entity.closerThan(p, 16.0D))
                .filter((p) -> isEntityTargetable(entity, p))
                .filter((p) -> p.getCapability(ReputationHandler.REPUTATION_CAPABILITY).isPresent())
                .sorted(Comparator.comparingDouble(entity::distanceToSqr)).toList();
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
        if(ModDefinitions.INJURED_FLEEING_ENTITIES.contains(entity.getType()) && entity instanceof Mob mob) {
            float percent = mob.getHealth()/mob.getMaxHealth();
            if(this.player!=null || mob.getLastHurtByMob() instanceof Player ) {
                if(mob.getLastHurtByMob() instanceof Player p) this.player = p;
                if (mob.distanceTo(this.player)<=28 && percent <= .5f) {
                    boolean inFaction = ReputationHandler.getEntityFactions(mob).isEmpty();
                    for (Faction f : ReputationHandler.getEntityFactions(mob)) {
                        if (PlayerFactionHandler.isPlayerInFaction(this.player, f)) inFaction = true;
                    }
                    if (!inFaction && this.random.nextFloat(21f)>=2f && !this.startFlee) {
                        this.startFlee = true;
                        if (this.player instanceof ServerPlayer)
                            PacketHandler.sendTo(new FleeIconMessage(mob.getUUID(), true), (ServerPlayer) this.player);
                        else if (!RenderEvents.fleeingMobs.contains(mob.getUUID()))
                            RenderEvents.fleeingMobs.add(mob.getUUID());
                    }
                    if(this.startFlee && WorldEvents.trackers.containsKey(mob)) {
                        ChatTracker tracker = WorldEvents.trackers.get(mob);
                        if(!tracker.getRecent() && !tracker.getFlee() && ServerTrackers.hasIconsForEvent(tracker.getEntityType(),"flee")) {
                            tracker.setFlee(true);
                            tracker.setChanged(true);
                            tracker.setRecent(true);
                        }
                    }
                }
                else if(this.startFlee) {
                    this.startFlee = false;
                    if (this.player instanceof ServerPlayer)
                        PacketHandler.sendTo(new FleeIconMessage(mob.getUUID(), false), (ServerPlayer) this.player);
                    else RenderEvents.fleeingMobs.remove(mob.getUUID());
                    if(percent <= .5f) {
                        for (Faction f : ReputationHandler.getEntityFactions(mob)) {
                            ReputationHandler.changeReputation(this.player, f, -1 * f.getActionWeighting("fleeing"));
                        }
                        mob.discard();
                    }
                }
            }
            else if(this.startFlee) {
                this.startFlee = false;
                if (this.player!=null && this.player instanceof ServerPlayer)
                    PacketHandler.sendTo(new FleeIconMessage(mob.getUUID(), false), (ServerPlayer) this.player);
                else RenderEvents.fleeingMobs.remove(mob.getUUID());
            }
            if(this.startFlee) entity.getBrain().setMemory(ReputationMemoryModule.FLEE_FROM_PLAYER.get(), this.player);
            else entity.getBrain().setMemory(ReputationMemoryModule.FLEE_FROM_PLAYER.get(), Optional.empty());
        }
    }
}
