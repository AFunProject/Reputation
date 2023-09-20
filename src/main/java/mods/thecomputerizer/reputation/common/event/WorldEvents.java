package mods.thecomputerizer.reputation.common.event;

import mods.thecomputerizer.reputation.capability.Faction;
import mods.thecomputerizer.reputation.capability.handlers.PlayerFactionHandler;
import mods.thecomputerizer.reputation.capability.handlers.ReputationHandler;
import mods.thecomputerizer.reputation.capability.reputation.IReputation;
import mods.thecomputerizer.reputation.Constants;
import mods.thecomputerizer.reputation.capability.reputation.ReputationProvider;
import mods.thecomputerizer.reputation.common.ai.ChatTracker;
import mods.thecomputerizer.reputation.common.ai.ReputationAIPackages;
import mods.thecomputerizer.reputation.common.ai.ReputationStandings;
import mods.thecomputerizer.reputation.common.ai.ServerTrackers;
import mods.thecomputerizer.reputation.common.ai.goals.*;
import mods.thecomputerizer.reputation.capability.playerfaction.PlayerFactionProvider;
import mods.thecomputerizer.reputation.common.command.ReputationCommands;
import mods.thecomputerizer.reputation.network.PacketChatIcon;
import mods.thecomputerizer.reputation.network.PacketSyncFactions;
import mods.thecomputerizer.reputation.util.HelperMethods;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("rawtypes")
@Mod.EventBusSubscriber(modid = Constants.MODID)
public class WorldEvents {

    public static final Map<LivingEntity, ChatTracker> TRACKER_MAP = new ConcurrentHashMap<>();
    private static final List<ServerPlayer> PLAYERS = new ArrayList<>();
    private static int TICK_COUNTER = 0;

    /**
     * Attach player faction capability to the overworld
     */
    @SubscribeEvent
    public static void attachLevelCapabilities(AttachCapabilitiesEvent<Level> event) {
        Level level = event.getObject();
        if(level.dimension()==Level.OVERWORLD && !level.isClientSide()) {
            for(Faction faction : ReputationHandler.getFactionMap().values()) {
                PlayerFactionProvider playerFactionProvider = new PlayerFactionProvider(faction);
                PlayerFactionHandler.PLAYER_FACTIONS.put(faction,playerFactionProvider);
                event.addCapability(faction.getID(),playerFactionProvider);
            }
        }
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        ReputationCommands.register(event.getDispatcher());
    }

    /**
     * Handles AI tweaks for entities that spawn for both entities that use brains and those that do not.
     */
    @SubscribeEvent
    public static void onJoin(EntityJoinWorldEvent event) {
        if(event.getEntity() instanceof LivingEntity entity && !event.getWorld().isClientSide) {
            Brain<? extends LivingEntity> brain = entity.getBrain();
            //only need to add sensors to mobs that actually use brains, otherwise goals are necessary
            if(!brain.memories.isEmpty()) {
                brain.sensors.putIfAbsent(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_LIVING_ENTITIES.create());
                brain.memories.putIfAbsent(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, Optional.empty());
                ReputationAIPackages.buildReputationSensor(brain);
            }
            if(entity instanceof ServerPlayer player) {
                //sync faction and chat icon data to players upon joining
                IReputation reputation = ReputationHandler.getCapability(player);
                if(Objects.nonNull(reputation)) {
                    Map<Faction, Integer> toSync = reputation.allReputations();
                    new PacketSyncFactions(toSync,ReputationAIPackages.STANDINGS.getData()).addPlayers(player).send();
                    for(Faction f : toSync.keySet()) reputation.setReputation(player,f,toSync.get(f));
                    ServerTrackers.syncChatIcons(player);
                    PLAYERS.add(player);
                }
            }
            else {
                //reputation based AI stuff
                //makes hostile mobs not a part of the player's faction have a chance to flee from battle when injured
                if(ReputationStandings.INJURED_FLEEING_ENTITIES.contains(entity.getType())) {
                    if(!brain.memories.isEmpty())
                        ReputationAIPackages.buildReputationInjuredAI(brain,0.5f);
                    else if(entity instanceof PathfinderMob mob) {
                        FleeBattleGoal fleeBattleGoal = new FleeBattleGoal(mob,0.5f);
                        mob.goalSelector.addGoal(0,fleeBattleGoal);
                        mob.targetSelector.addGoal(0,new FleeBattleTargetOverride(fleeBattleGoal,mob));
                    }
                }
                //makes passive mobs flee from players with specific reputation standings
                if(ReputationStandings.PASSIVE_FLEEING_ENTITIES.contains(entity.getType())) {
                    if(!brain.memories.isEmpty())
                        ReputationAIPackages.buildReputationFleeAI(brain,HelperMethods.fleeFactor(entity),
                                ReputationAIPackages.STANDINGS.getPassiveFleeing(entity.getType()));
                    else if(entity instanceof Mob mob)
                        mob.goalSelector.addGoal(0,new FleeGoal(mob,HelperMethods.fleeFactor(entity),true));
                }
                //makes neutral mobs angry at players based on reputation standings
                if(ReputationStandings.HOSTILE_ENTITIES.contains(entity.getType()) && entity instanceof Mob mob) {
                    if(!brain.memories.isEmpty())
                        ReputationAIPackages.buildReputationHostileAI(mob,brain,
                                ReputationAIPackages.STANDINGS.getHostile(entity.getType()));
                    else if(entity instanceof NeutralMob)
                        mob.targetSelector.addGoal(2,new ReputationAttackableTargetGoal<>(mob,Player.class,true,false));
                }
                //conditionally removes players from the target selection of hostile mobs based on reputation standings
                if(ReputationStandings.PASSIVE_ENTITIES.contains(entity.getType())) {
                    if(!brain.memories.isEmpty())
                        ReputationAIPackages.buildReputationPassiveAI(brain,1,
                                ReputationAIPackages.STANDINGS.getPassive(entity.getType()));
                    else if(entity instanceof Mob mob) {
                        Set<WrappedGoal> goalSet = mob.targetSelector.getAvailableGoals();
                        List<WrappedGoal> newGoals = goalSet.stream()
                                .filter((g) -> {
                                    if(g.getGoal() instanceof NearestAttackableTargetGoal targetGoal)
                                        return targetGoal.targetType != Player.class && targetGoal.targetType != ServerPlayer.class;
                                    return false;
                                }).toList();
                        mob.targetSelector.removeAllGoals();
                        for(WrappedGoal wrappedGoal : newGoals)
                            mob.targetSelector.addGoal(wrappedGoal.getPriority(),wrappedGoal.getGoal());
                        mob.targetSelector.addGoal(2, new ReputationPacifyHostileCustomStandingGoal<>(mob,
                                Player.class,true,false,ReputationAIPackages.STANDINGS.getPassive(entity.getType())));
                    }
                }
            }
            //finalize trackers for chat icons
            synchronized (TRACKER_MAP) {
                if (ServerTrackers.hasAnyIcons(entity.getType()))
                    TRACKER_MAP.put(entity,new ChatTracker(entity));
            }
        }
    }

    /**
     * Synchronize capabilities for respawning players
     */
    @SubscribeEvent
    public static void onRespawn(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player respawned = event.getPlayer();
        if(original instanceof ServerPlayer sOriginal && respawned instanceof ServerPlayer sRespawned) {
            PLAYERS.remove(sOriginal);
            PLAYERS.add(sRespawned);
        }
        Brain<?> brain = respawned.getBrain();
        brain.sensors.putIfAbsent(SensorType.NEAREST_LIVING_ENTITIES,SensorType.NEAREST_LIVING_ENTITIES.create());
        brain.memories.putIfAbsent(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,Optional.empty());
        original.reviveCaps();
        for(Faction f : ReputationHandler.getFactionMap().values())
            respawned.getCapability(ReputationProvider.REPUTATION_CAPABILITY).orElseThrow(RuntimeException::new)
                    .setReputation(respawned,f,ReputationHandler.getReputation(original,f));
        original.invalidateCaps();
    }

    /**
     * Stop keeping track of players that log out
     */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent e) {
        if(e.getPlayer() instanceof ServerPlayer player)
            PLAYERS.remove(player);
    }

    /**
     * Cleared cached maps when the server closes. Helpful for singleplayer world and guarding against memory leaks
     */
    @SubscribeEvent
    public static void onServerClose(ServerStoppedEvent e) {
        ReputationHandler.emptyMaps();
        PLAYERS.clear();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if(e.phase== TickEvent.Phase.END) {
            if(ServerTrackers.hasAnyIcons()) {
                //synchronize the tracker map to stop cmod errors
                synchronized (TRACKER_MAP) {
                    TICK_COUNTER++;
                    for (ChatTracker tracker : TRACKER_MAP.values())
                        tracker.queryChatTimer();
                    //only check and sync chat icon trackers once a second for performance purposes
                    if(TICK_COUNTER>=20) {
                        List<ChatTracker> toUpdate = new ArrayList<>();
                        for(LivingEntity entity : TRACKER_MAP.keySet()) {
                            if(ServerTrackers.rollRandom(entity.getType())) {
                                ChatTracker tracker = TRACKER_MAP.get(entity);
                                if(tracker.notRecent()) {
                                    //check and set the idle event
                                    if(!tracker.getRandom() && ServerTrackers.hasIconsForEvent(tracker.getEntityType(),"idle")) {
                                        tracker.setRandom(true);
                                        tracker.setChanged(true);
                                        tracker.setRecent(true);
                                    }
                                    //check and set the idle_faction event
                                    if(!tracker.getInRange() && ServerTrackers.hasIconsForEvent(tracker.getEntityType(),"idle_faction")) {
                                        Level level = entity.getLevel();
                                        if(level instanceof ServerLevel serverLevel) {
                                            for(Faction f : ReputationHandler.getEntityFactions(entity)) {
                                                if(!HelperMethods.getSeenEntitiesOfFaction(serverLevel,entity,10,entity.getBrain(),f).isEmpty()) {
                                                    tracker.setInRange(true);
                                                    tracker.setChanged(true);
                                                    tracker.setRecent(true);
                                                }
                                            }
                                        }
                                    }
                                    if(tracker.getChanged()) toUpdate.add(tracker);
                                }
                            }
                        }
                        //only sync chat icon trackers with changes to the client for performance purposes
                        if(!toUpdate.isEmpty()) {
                            if(!PLAYERS.isEmpty())
                                new PacketChatIcon(toUpdate).addPlayers(PLAYERS.toArray(new ServerPlayer[]{})).send();
                            for(ChatTracker tracker : toUpdate) {
                                tracker.setRandom(false);
                                tracker.setInRange(false);
                                tracker.setEngage(false);
                                tracker.setFlee(false);
                                tracker.setChanged(false);
                            }
                        }
                        TICK_COUNTER = 0;
                    }
                }
            }
        }
    }
}
