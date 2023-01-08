package mods.thecomputerizer.reputation.api;

import mods.thecomputerizer.reputation.api.capability.IPlayerFaction;
import mods.thecomputerizer.reputation.common.capability.PlayerFactionProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PlayerFactionHandler {

    public static Map<Faction, PlayerFactionProvider> PLAYER_FACTIONS = new HashMap<>();

    public static IPlayerFaction getCapability(Faction faction) {
        LazyOptional<IPlayerFaction> optional =
                ServerLifecycleHooks.getCurrentServer().overworld().getCapability(PLAYER_FACTIONS.get(faction).PLAYER_FACTION);
        return optional.isPresent() ? optional.resolve().isPresent() ? optional.resolve().get() : null : null;
    }

    public static boolean isPlayerInFaction(Player player, Faction faction) {
        IPlayerFaction playerFaction = getCapability(faction);
        if(Objects.isNull(playerFaction)) return false;
        return playerFaction.isPlayerAttached(player);
    }

    public static boolean addPlayerToFaction(Player player, Faction faction) {
        boolean pass = false;
        for(Faction other : ReputationHandler.getFactionMap().values()) {
            if(other==faction) {
                IPlayerFaction playerFaction = getCapability(faction);
                if (Objects.nonNull(playerFaction)) pass = playerFaction.addPlayer(player);
            } else removePlayerFromFaction(player,faction);
        }
        return pass;
    }

    public static boolean removePlayerFromFaction(Player player, Faction faction) {
        IPlayerFaction playerFaction = getCapability(faction);
        if (Objects.nonNull(playerFaction)) return playerFaction.removePlayer(player);
        return false;
    }
}
