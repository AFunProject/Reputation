package mods.thecomputerizer.reputation.util;

import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import mods.thecomputerizer.reputation.common.ai.ReputationAIPackages;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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

    public static double tradePrices(LivingEntity entity, Player player) {
        Collection<Faction> factions = ReputationHandler.getEntityFactions(entity);
        if(!factions.isEmpty() && ModDefinitions.TRADING_ENTITIES.contains(entity.getType())) {
            int total = 0;
            int rep = 0;
            int cutoff = 0;
            for (Faction f : factions) {
                rep += ReputationHandler.getReputation(player, f);
                total++;
                if(ReputationAIPackages.trading_standings.get(entity.getType()).matches("bad")) cutoff+=f.getLowerRep();
                else cutoff+=f.getHigherRep();
            }
            float averageHighRep = cutoff/(float)total;
            if(rep!=averageHighRep) return (((double)rep/total)-averageHighRep)/(averageHighRep*-5);
        }
        return 0d;
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

    public static List<? extends LivingEntity> getSeenEntitiesOfFaction(Brain<? extends LivingEntity> brain, Faction faction) {
        List<LivingEntity> ret = new ArrayList<>();
        Optional<NearestVisibleLivingEntities> optional = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        optional.ifPresent(nearestVisibleLivingEntities -> nearestVisibleLivingEntities.findAll(faction::isMember).iterator().forEachRemaining((ret::add)));
        return ret;
    }

    public static List<? extends LivingEntity> getEntitiesOfFactionNearPlayer(ServerLevel level, Player player, Faction faction, int range) {
        List<LivingEntity> ret = level.getEntitiesOfClass(LivingEntity.class, new AABB(player.getX() - range, player.getY() - (range / 2f), player.getZ() - range, player.getX() + range, player.getY() + (range / 2f), player.getZ() + range));
        return ret.stream().filter(entity -> faction.getMembers().contains(entity.getType())).collect(Collectors.toList());
    }

    public static List<? extends LivingEntity> getSeenEntitiesOfTypeInRange(ServerLevel level, LivingEntity entity, EntityType<?> type, BlockPos pos, double range) {
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(pos.getX()-range, pos.getY()-(range/2), pos.getZ()-range, pos.getX()+range, pos.getY()+(range/2), pos.getZ()+range)).stream()
                .filter(e -> e.getType()==type)
                .filter(e -> e.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).isPresent() && e.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().contains(entity))
                .collect(Collectors.toList());
    }

    public static int getOneAboveIntegerIndexOfMapForValue(Map<?, ?> map, List<?> valueAsList) {
        for (int i = 0; i < map.values().size(); i++)
            if (map.values().stream().toList().get(i) == valueAsList.get(0)) return i+1;
        return 0;
    }
}
