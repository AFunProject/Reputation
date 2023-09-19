package mods.thecomputerizer.reputation.common.ai;

import com.google.common.collect.ImmutableSet;
import mods.thecomputerizer.reputation.Constants;
import mods.thecomputerizer.reputation.capability.Faction;
import mods.thecomputerizer.reputation.capability.handlers.PlayerFactionHandler;
import mods.thecomputerizer.reputation.capability.handlers.ReputationHandler;
import mods.thecomputerizer.reputation.capability.reputation.ReputationProvider;
import mods.thecomputerizer.reputation.client.ClientEvents;
import mods.thecomputerizer.reputation.common.event.WorldEvents;
import mods.thecomputerizer.reputation.network.PacketFleeIcon;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ReputationSensor extends Sensor<LivingEntity> {
    
    private boolean startFlee = false;
    private Player player = null;


    @Override
    public @NotNull Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(ReputationMemoryModule.NEAREST_PLAYER_BAD_REPUTATION.get(),
                ReputationMemoryModule.NEAREST_PLAYER_NEUTRAL_REPUTATION.get(),
                ReputationMemoryModule.NEAREST_PLAYER_GOOD_REPUTATION.get(),ReputationMemoryModule.FLEE_FROM_PLAYER.get());
    }

    @Override
    protected void doTick(ServerLevel level, LivingEntity entity) {
        Brain<?> brain = entity.getBrain();
        List<ServerPlayer> list = level.players().stream().filter(EntitySelector.NO_SPECTATORS)
                .filter(p -> entity.closerThan(p,16d))
                .filter(p -> isEntityTargetable(entity,p))
                .filter(p -> p.getCapability(ReputationProvider.REPUTATION_CAPABILITY).isPresent())
                .sorted(Comparator.comparingDouble(entity::distanceToSqr)).toList();
        if(!list.isEmpty()) {
            Player nearest = list.get(0);
            for (Faction f : ReputationHandler.getEntityFactions(entity)) {
                int reputation = ReputationHandler.getReputation(nearest,f);
                if(reputation<=f.getLowerRep()) {
                    brain.setMemory(ReputationMemoryModule.NEAREST_PLAYER_BAD_REPUTATION.get(),nearest);
                    brain.setMemory(ReputationMemoryModule.NEAREST_PLAYER_NEUTRAL_REPUTATION.get(),Optional.empty());
                    brain.setMemory(ReputationMemoryModule.NEAREST_PLAYER_GOOD_REPUTATION.get(),Optional.empty());
                }
                else if(reputation<f.getHigherRep()) {
                    brain.setMemory(ReputationMemoryModule.NEAREST_PLAYER_NEUTRAL_REPUTATION.get(),nearest);
                    brain.setMemory(ReputationMemoryModule.NEAREST_PLAYER_BAD_REPUTATION.get(),Optional.empty());
                    brain.setMemory(ReputationMemoryModule.NEAREST_PLAYER_GOOD_REPUTATION.get(),Optional.empty());
                }
                else {
                    brain.setMemory(ReputationMemoryModule.NEAREST_PLAYER_GOOD_REPUTATION.get(),nearest);
                    brain.setMemory(ReputationMemoryModule.NEAREST_PLAYER_BAD_REPUTATION.get(),Optional.empty());
                    brain.setMemory(ReputationMemoryModule.NEAREST_PLAYER_NEUTRAL_REPUTATION.get(),Optional.empty());
                }
            }
        }
        if(ReputationStandings.INJURED_FLEEING_ENTITIES.contains(entity.getType()) && entity instanceof Mob mob) {
            float percent = mob.getHealth()/mob.getMaxHealth();
            if(Objects.nonNull(this.player) || mob.getLastHurtByMob() instanceof Player ) {
                if(mob.getLastHurtByMob() instanceof Player p) this.player = p;
                if(mob.distanceTo(this.player)<=28 && percent <= .5f) {
                    boolean inFaction = ReputationHandler.getEntityFactions(mob).isEmpty();
                    for(Faction f : ReputationHandler.getEntityFactions(mob))
                        if(PlayerFactionHandler.isPlayerInFaction(this.player, f))
                            inFaction = true;
                    if(!inFaction && Constants.floatRand(21f)>=2f && !this.startFlee) {
                        this.startFlee = true;
                        if(this.player instanceof ServerPlayer sPlayer)
                            new PacketFleeIcon(mob.getUUID(),true).addPlayers(sPlayer).send();
                        else if (!ClientEvents.FLEEING_MOBS.contains(mob.getUUID()))
                            ClientEvents.FLEEING_MOBS.add(mob.getUUID());
                    }
                    synchronized (WorldEvents.TRACKER_MAP) {
                        if (this.startFlee && WorldEvents.TRACKER_MAP.containsKey(mob)) {
                            ChatTracker tracker = WorldEvents.TRACKER_MAP.get(mob);
                            if (tracker.notRecent() && tracker.notFlee() && ServerTrackers.hasIconsForEvent(tracker.getEntityType(), "flee")) {
                                tracker.setFlee(true);
                                tracker.setChanged(true);
                                tracker.setRecent(true);
                            }
                        }
                    }
                }
                else if(this.startFlee) {
                    this.startFlee = false;
                    if (this.player instanceof ServerPlayer sPlayer)
                        new PacketFleeIcon(mob.getUUID(),false).addPlayers(sPlayer).send();
                    else ClientEvents.FLEEING_MOBS.remove(mob.getUUID());
                    if(percent<=0.5f) {
                        for(Faction f : ReputationHandler.getEntityFactions(mob))
                            ReputationHandler.changeReputation(this.player,f,-1*f.getActionWeighting("fleeing"));
                        mob.discard();
                    }
                }
            }
            else if(this.startFlee) {
                this.startFlee = false;
                if (this.player instanceof ServerPlayer sPlayer)
                    new PacketFleeIcon(mob.getUUID(),false).addPlayers(sPlayer).send();
                else ClientEvents.FLEEING_MOBS.remove(mob.getUUID());
            }
            if(this.startFlee) brain.setMemory(ReputationMemoryModule.FLEE_FROM_PLAYER.get(),this.player);
            else brain.setMemory(ReputationMemoryModule.FLEE_FROM_PLAYER.get(),Optional.empty());
        }
    }
}
