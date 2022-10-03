package mods.thecomputerizer.reputation.util;

import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.PlayerFactionHandler;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import mods.thecomputerizer.reputation.common.ai.ReputationAIPackages;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.*;
import java.util.stream.Collectors;

public class HelperMethods {

    public static Player getNearestPlayerInCustomStandingToEntity(LivingEntity entity, double distance, String reputationStanding) {
        if(reputationStanding.matches("good")) return getNearestPlayerInGoodStandingToEntity(entity, distance);
        else if(reputationStanding.matches("neutral")) return getNearestPlayerInNeutralStandingToEntity(entity, distance);
        return getNearestPlayerInBadStandingToEntity(entity, distance);
    }

    public static Player getNearestPlayerInGoodStandingToEntity(LivingEntity entity, double distance) {
        Level level = entity.level;
        List<? extends Player> list = level.players().stream().filter(EntitySelector.NO_SPECTATORS)
                .filter((p) -> entity.closerThan(p, distance))
                .filter((p) -> p.getCapability(ReputationHandler.REPUTATION_CAPABILITY).isPresent())
                .filter((p) -> isPlayerInGoodStanding(entity, p))
                .sorted(Comparator.comparingDouble(entity::distanceToSqr)).toList();
        if(!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public static Player getNearestPlayerInNeutralStandingToEntity(LivingEntity entity, double distance) {
        Level level = entity.level;
        List<? extends Player> list = level.players().stream().filter(EntitySelector.NO_SPECTATORS)
                .filter((p) -> entity.closerThan(p, distance))
                .filter((p) -> p.getCapability(ReputationHandler.REPUTATION_CAPABILITY).isPresent())
                .filter((p) -> isPlayerInNeutralStanding(entity, p))
                .sorted(Comparator.comparingDouble(entity::distanceToSqr)).toList();
        if(!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public static Player getNearestPlayerInBadStandingToEntity(LivingEntity entity, double distance) {
        Level level = entity.level;
        List<? extends Player> list = level.players().stream().filter(EntitySelector.NO_SPECTATORS)
                .filter((p) -> entity.closerThan(p, distance))
                .filter((p) -> p.getCapability(ReputationHandler.REPUTATION_CAPABILITY).isPresent())
                .filter((p) -> isPlayerInBadStanding(entity, p))
                .sorted(Comparator.comparingDouble(entity::distanceToSqr)).toList();
        if(!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public static boolean ensureSeparateFactions(LivingEntity entity, Player player) {
        for(Faction f : ReputationHandler.getEntityFactions(entity)) {
            if(PlayerFactionHandler.isPlayerInFaction(f,player)) return false;
        }
        return true;
    }

    public static boolean isPlayerInCustomStanding(LivingEntity entity, Player player, String reputationStanding) {
        if(reputationStanding.matches("good")) return isPlayerInGoodStanding(entity, player);
        else if(reputationStanding.matches("neutral")) return isPlayerInNeutralStanding(entity, player);
        return isPlayerInBadStanding(entity, player);
    }

    public static boolean isPlayerInGoodStanding(LivingEntity entity, Player player) {
        for(Faction faction : ReputationHandler.getEntityFactions(entity)) {
            if(ReputationHandler.getReputation(player,faction)>=faction.getHigherRep()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPlayerInNeutralStanding(LivingEntity entity, Player player) {
        for(Faction faction : ReputationHandler.getEntityFactions(entity)) {
            if(ReputationHandler.getReputation(player,faction)>faction.getLowerRep() && ReputationHandler.getReputation(player,faction)<faction.getHigherRep()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPlayerInBadStanding(LivingEntity entity, Player player) {
        for(Faction faction : ReputationHandler.getEntityFactions(entity)) {
            if(ReputationHandler.getReputation(player,faction)<=faction.getLowerRep()) {
                return true;
            }
        }
        return false;
    }

    public static double tradePrice(float percentage, int initialCount, int stackSize) {
        if(percentage<=.5f) return 999d;
        else if(percentage>=1f && percentage<1.5f) return 1d;
        else if(percentage>.5 && percentage<1) {
            float maxStackSizePercent = ((float) stackSize/(float) initialCount);
            float itemPercentage = 1f-((percentage-.5f)*2f);
            return 1d+((maxStackSizePercent-1f)*itemPercentage);
        }
        double exponential = 1f/(percentage-0.5f);
        return Math.pow(2d,exponential)/2d;
    }

    public static float tradeMultiplier(LivingEntity entity, Player player) {
        Collection<Faction> factions = ReputationHandler.getEntityFactions(entity);
        if(!factions.isEmpty() && ModDefinitions.TRADING_ENTITIES.contains(entity.getType())) {
            Faction lowest = null;
            for (Faction f : factions) {
                if(lowest==null) lowest = f;
                else if(getPercentageAwayFromLowerBound(f,player)<getPercentageAwayFromLowerBound(lowest,player)) lowest = f;
            }
            if(lowest!=null) {
                float percentage = getPercentageAwayFromLowerBound(lowest,player);
                if(ReputationAIPackages.standings.getTrading(entity.getType()).matches("bad")) return percentage;
                else if (ReputationAIPackages.standings.getTrading(entity.getType()).matches("neutral")) return percentage-0.5f;
                else return percentage-1f;
            }
        }
        return 1f;
    }

    public static float getPercentageAwayFromLowerBound(Faction faction, Player player) {
        float lower = faction.getLowerRep();
        float higher = faction.getHigherRep();
        float difference = Math.abs(Math.subtractExact((int) higher,(int) lower));
        float currentReputation = ReputationHandler.getReputation(player,faction);
        if(currentReputation==lower) return 0f;
        float currentDifference = Math.abs(Math.subtractExact((int) lower,(int) currentReputation));
        float percentage = currentDifference/difference;
        if(currentReputation<lower) return -1f*percentage;
        return percentage;
    }

    public static float fleeFactor(LivingEntity entity) {
        Player player = getNearestPlayerInBadStandingToEntity(entity,16d);
        if(player!=null) {
            Collection<Faction> factions = ReputationHandler.getEntityFactions(entity);
            if (!factions.isEmpty()) {
                int total = 0;
                int rep = 0;
                int cutoff = 0;
                for (Faction f : factions) {
                    rep += ReputationHandler.getReputation(player, f);
                    total++;
                    cutoff+=f.getLowerRep();
                }
                float averageLowRep = cutoff/(float)total;
                if (rep != averageLowRep) return 0.5f - (((float) rep / total) + Math.abs(averageLowRep)) / (2*Math.abs(averageLowRep));
            }
        }
        return 1f;
    }

    public static List<? extends LivingEntity> getSeenEntitiesOfFaction(ServerLevel level, LivingEntity mob, int range, Brain<? extends LivingEntity> brain, Faction faction) {
        List<LivingEntity> ret = new ArrayList<>();
        if(brain.memories.isEmpty()) {
            ret.addAll(level.getEntitiesOfClass(LivingEntity.class, new AABB(mob.getX() - range, mob.getY() - (range / 2f), mob.getZ() - range, mob.getX() + range, mob.getY() + (range / 2f), mob.getZ() + range)));
            ret.removeIf(e -> {
                if(e.getStringUUID().matches(mob.getStringUUID()) || !faction.getMembers().contains(e.getType())) return true;
                return e instanceof Mob m && !m.getSensing().hasLineOfSight(mob);
            });
        } else {
            try {
                Optional<NearestVisibleLivingEntities> optional = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
                optional.ifPresent(nearestVisibleLivingEntities -> nearestVisibleLivingEntities.findAll(faction::isMember).iterator().forEachRemaining((ret::add)));
            } catch (Exception e) {
                Reputation.logError("Failed to collect seen entities using a brain",e);
            }
        }
        return ret;
    }

    public static List<? extends LivingEntity> getNearEntitiesOfFaction(ServerLevel level, LivingEntity entity, Faction faction, int range) {
        List<LivingEntity> ret = level.getEntitiesOfClass(LivingEntity.class, new AABB(entity.getX() - range, entity.getY() - (range / 2f), entity.getZ() - range, entity.getX() + range, entity.getY() + (range / 2f), entity.getZ() + range));
        return ret.stream().filter(e -> !e.getStringUUID().matches(entity.getStringUUID()) && faction.getMembers().contains(e.getType())).collect(Collectors.toList());
    }

    public static List<? extends LivingEntity> getSeenEntitiesOfTypeInRange(ServerLevel level, LivingEntity entity, EntityType<?> type, BlockPos pos, double range) {
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(pos.getX()-range, pos.getY()-(range/2), pos.getZ()-range, pos.getX()+range, pos.getY()+(range/2), pos.getZ()+range)).stream()
                .filter(e -> e.getType()==type && e!=entity)
                .filter(e -> {
                    if(!e.getBrain().memories.isEmpty()) return e.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).isPresent() && e.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().contains(entity);
                    else return e instanceof Mob mob && mob.getSensing().hasLineOfSight(entity);
                })
                .collect(Collectors.toList());
    }

    //converts an element of a 2D array to its 1D counterpart
    public static int flatIndex(int width, int x, int y) {
        return width*y+x;
    }
}
