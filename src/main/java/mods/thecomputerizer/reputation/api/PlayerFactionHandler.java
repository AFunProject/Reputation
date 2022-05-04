package mods.thecomputerizer.reputation.api;

import mods.thecomputerizer.reputation.api.capability.IPlayerFaction;
import mods.thecomputerizer.reputation.common.capability.PlayerFactionProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class PlayerFactionHandler {

    public static Map<Faction, PlayerFactionProvider> PLAYER_FACTIONS = new HashMap<>();

    public static boolean isPlayerInFaction(Faction f, Player p) {
        LazyOptional<IPlayerFaction> optional = ServerLifecycleHooks.getCurrentServer().overworld().getCapability(PLAYER_FACTIONS.get(f).PLAYER_FACTION);
        if (optional.isPresent()) {
            IPlayerFaction playerFaction = optional.resolve().get();
            return playerFaction.isPlayerAttached(p);
        }
        return false;
    }

    public static boolean addPlayerToFaction(Faction f, Player p) {
        boolean pass = false;
        for(Faction other : ReputationHandler.getFactionMap().values()) {
            if(other==f) {
                LazyOptional<IPlayerFaction> optional = ServerLifecycleHooks.getCurrentServer().overworld().getCapability(PLAYER_FACTIONS.get(f).PLAYER_FACTION);
                if (optional.isPresent()) {
                    IPlayerFaction playerFaction = optional.resolve().get();
                    pass = playerFaction.addPlayer(p);
                }
            } else removePlayerFromFaction(other, p);
        }
        return pass;
    }

    public static boolean removePlayerFromFaction(Faction f, Player p) {
        LazyOptional<IPlayerFaction> optional = ServerLifecycleHooks.getCurrentServer().overworld().getCapability(PLAYER_FACTIONS.get(f).PLAYER_FACTION);
        if (optional.isPresent()) {
            IPlayerFaction playerFaction = optional.resolve().get();
            return playerFaction.removePlayer(p);
        }
        return false;
    }
}
