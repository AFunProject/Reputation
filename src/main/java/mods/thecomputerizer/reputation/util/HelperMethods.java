package mods.thecomputerizer.reputation.util;

import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.capability.Faction;
import mods.thecomputerizer.reputation.capability.handlers.PlayerFactionHandler;
import mods.thecomputerizer.reputation.capability.handlers.ReputationHandler;
import mods.thecomputerizer.reputation.ReputationRef;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static mods.thecomputerizer.reputation.capability.reputation.ReputationProvider.REPUTATION_CAPABILITY;
import static mods.thecomputerizer.reputation.common.ai.ReputationAIPackages.STANDINGS;
import static mods.thecomputerizer.reputation.common.ai.ReputationStandings.TRADING_ENTITIES;
import static net.minecraft.world.entity.EntitySelector.NO_SPECTATORS;
import static net.minecraft.world.entity.ai.memory.MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES;

public class HelperMethods {
    
    public static boolean ensureSeparateFactions(LivingEntity entity, Player player) {
        for(Faction f : ReputationHandler.getEntityFactions(entity))
            if(PlayerFactionHandler.isPlayerInFaction(player,f)) return false;
        return true;
    }
    
    /**
     * Converts an element of a 2D array to its 1D counterpart
     */
    public static int flatIndex(int width, int x, int y) {
        return width*y+x;
    }
    
    public static float fleeFactor(LivingEntity entity) {
        Player player = getNearestPlayerInBadStandingToEntity(entity,16d);
        if(Objects.nonNull(player)) {
            Collection<Faction> factions = ReputationHandler.getEntityFactions(entity);
            if(!factions.isEmpty()) {
                int total = 0;
                int rep = 0;
                int cutoff = 0;
                for(Faction f : factions) {
                    rep += ReputationHandler.getReputation(player,f);
                    total++;
                    cutoff+=f.getLowerRep();
                }
                float averageLowRep = cutoff/(float)total;
                if(rep!=averageLowRep)
                    return 0.5f-(((float)rep/total)+Math.abs(averageLowRep))/(2*Math.abs(averageLowRep));
            }
        }
        return 1f;
    }
    
    public static AABB getBox(double x, double y, double z, double range) {
        double halfRange = range/2;
        return new AABB(x-range,y-halfRange,z-range,x+range,y+halfRange,z+range);
    }
    
    public static List<? extends LivingEntity> getNearEntitiesOfFaction(ServerLevel level, LivingEntity entity,
            Faction faction, int range) {
        List<LivingEntity> ret = level.getEntitiesOfClass(LivingEntity.class,
                getBox(entity.getX(),entity.getY(),entity.getZ(),range));
        return ret.stream().filter(e -> !e.getStringUUID().matches(entity.getStringUUID()) &&
                                        faction.getMembers().contains(e.getType())).collect(Collectors.toList());
    }
    
    public static Player getNearestPlayerInBadStandingToEntity(LivingEntity entity, double distance) {
        return getNearestPlayerToEntityWithFilter(entity,distance,HelperMethods::isPlayerInBadStanding);
    }

    public static Player getNearestPlayerInCustomStandingToEntity(LivingEntity entity, double distance, String standing) {
        return switch(standing) {
            case "good" -> getNearestPlayerInGoodStandingToEntity(entity,distance);
            case "neutral" -> getNearestPlayerInNeutralStandingToEntity(entity,distance);
            default -> getNearestPlayerInBadStandingToEntity(entity,distance);
        };
    }

    public static Player getNearestPlayerInGoodStandingToEntity(LivingEntity entity, double distance) {
        return getNearestPlayerToEntityWithFilter(entity,distance,HelperMethods::isPlayerInGoodStanding);
    }

    public static Player getNearestPlayerInNeutralStandingToEntity(LivingEntity entity, double distance) {
        return getNearestPlayerToEntityWithFilter(entity,distance,HelperMethods::isPlayerInNeutralStanding);
    }
    
    public static Player getNearestPlayerToEntityWithFilter(LivingEntity entity, double distance,
            BiFunction<LivingEntity,Player,Boolean> filter) {
        Level level = entity.level;
        List<? extends Player> list = level.players().stream()
                .filter(NO_SPECTATORS)
                .filter(p -> entity.closerThan(p,distance))
                .filter(p -> p.getCapability(REPUTATION_CAPABILITY).isPresent())
                .filter(p -> filter.apply(entity,p))
                .sorted(Comparator.comparingDouble(entity::distanceToSqr))
                .toList();
        return list.isEmpty() ? null : list.get(0);
    }
    
    public static float getPercentageAwayFromLowerBound(Faction faction, Player player) {
        float lower = faction.getLowerRep();
        float higher = faction.getHigherRep();
        float difference = Math.abs(Math.subtractExact((int)higher,(int)lower));
        float currentRep = ReputationHandler.getReputation(player,faction);
        if(currentRep==lower) return 0f;
        float currentDifference = Math.abs(Math.subtractExact((int) lower,(int)currentRep));
        float percentage = currentDifference/difference;
        return currentRep<lower ? -1f*percentage : percentage;
    }
    
    public static List<? extends LivingEntity> getSeenEntitiesOfFaction(ServerLevel level, LivingEntity mob, int range,
            Brain<? extends LivingEntity> brain, Faction faction) {
        List<LivingEntity> ret = new ArrayList<>();
        if(brain.memories.isEmpty()) {
            ret.addAll(level.getEntitiesOfClass(LivingEntity.class,getBox(mob.getX(),mob.getY(),mob.getZ(),range)));
            ret.removeIf(e -> e.getUUID().compareTo(mob.getUUID())==0 ||
                              !faction.getMembers().contains(e.getType()) ||
                              (e instanceof Mob m && !m.getSensing().hasLineOfSight(mob)));
        } else {
            try {
                NearestVisibleLivingEntities nearest = brain.getMemory(NEAREST_VISIBLE_LIVING_ENTITIES).orElse(null);
                if(Objects.nonNull(nearest)) nearest.findAll(faction::isMember).iterator().forEachRemaining((ret::add));
            } catch(Exception ex) {
                Reputation.logError("Failed to collect seen entities using a brain",ex);
            }
        }
        return ret;
    }
    
    public static List<? extends LivingEntity> getSeenEntitiesOfTypeInRange(
            ServerLevel level, LivingEntity entity, EntityType<?> type, BlockPos pos, double range) {
        return level.getEntitiesOfClass(LivingEntity.class,getBox(pos.getX(),pos.getY(),pos.getZ(),range))
                .stream().filter(e -> e.getType()==type && e!=entity)
                .filter(e -> {
                    Brain<?> brain = e.getBrain();
                    if(!brain.memories.isEmpty())
                        return brain.getMemory(NEAREST_VISIBLE_LIVING_ENTITIES).isPresent() &&
                               brain.getMemory(NEAREST_VISIBLE_LIVING_ENTITIES).get().contains(entity);
                    else return e instanceof Mob mob && mob.getSensing().hasLineOfSight(entity);
                }).collect(Collectors.toList());
    }
    
    public static boolean isPlayerInBadStanding(LivingEntity entity, Player player) {
        for(Faction faction : ReputationHandler.getEntityFactions(entity))
            if(ReputationHandler.getReputation(player,faction)<=faction.getLowerRep()) return true;
        return false;
    }

    public static boolean isPlayerInCustomStanding(LivingEntity entity, Player player, String standing) {
        return switch(standing) {
            case "good" -> isPlayerInGoodStanding(entity,player);
            case "neutral" -> isPlayerInNeutralStanding(entity,player);
            default -> isPlayerInBadStanding(entity,player);
        };
    }

    public static boolean isPlayerInGoodStanding(LivingEntity entity, Player player) {
        for(Faction faction : ReputationHandler.getEntityFactions(entity))
            if(ReputationHandler.getReputation(player,faction)>=faction.getHigherRep()) return true;
        return false;
    }

    public static boolean isPlayerInNeutralStanding(LivingEntity entity, Player player) {
        for(Faction faction : ReputationHandler.getEntityFactions(entity)) {
            int reputation = ReputationHandler.getReputation(player,faction);
            if(reputation>faction.getLowerRep() && reputation<faction.getHigherRep()) return true;
        }
        return false;
    }

    public static float tradeMultiplier(LivingEntity entity, Player player) {
        Collection<Faction> factions = ReputationHandler.getEntityFactions(entity);
        EntityType<?> type = entity.getType();
        if(!factions.isEmpty() && TRADING_ENTITIES.contains(type)) {
            Faction lowest = null;
            for(Faction f : factions) {
                if(Objects.isNull(lowest)) lowest = f;
                else if(getPercentageAwayFromLowerBound(f,player)<getPercentageAwayFromLowerBound(lowest,player))
                    lowest = f;
            }
            if(Objects.nonNull(lowest)) {
                float percentage = getPercentageAwayFromLowerBound(lowest,player);
                return switch(STANDINGS.getTrading(type)) {
                    case "bad" -> percentage;
                    case "neutral" -> percentage-0.5f;
                    default -> percentage-1f;
                };
            }
        }
        return 1f;
    }
    
    public static double tradePrice(float percentage, int initialCount, int stackSize) {
        if(percentage<=0.5f) return 999d;
        if(percentage>=1f && percentage<1.5f) return 1d;
        if(percentage>0.5f && percentage<1f) {
            float maxStackSizePercent = ((float) stackSize/(float) initialCount);
            float itemPercentage = 1f-((percentage-0.5f)*2f);
            return 1d+((maxStackSizePercent-1f)*itemPercentage);
        }
        double exponential = 1f/(percentage-0.5f);
        return Math.pow(2d,exponential)/2d;
    }

    /**
     * Returns null if the list is empty
     */
    public static <T> T randomListElement(@Nonnull List<T> list) {
        int size = list.size();
        if(size==0) return null;
        return size==1 ? list.get(0) : list.get(ReputationRef.intRand(size));
    }
}