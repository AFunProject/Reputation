package mods.thecomputerizer.reputation.capability.handlers;

import mods.thecomputerizer.reputation.capability.Faction;
import mods.thecomputerizer.reputation.capability.playerfaction.IPlayerFaction;
import mods.thecomputerizer.reputation.capability.playerfaction.PlayerFactionProvider;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PlayerFactionHandler {

    public static Map<Faction, PlayerFactionProvider> PLAYER_FACTIONS = new HashMap<>();

    @SuppressWarnings("DataFlowIssue")
    private static @Nullable IPlayerFaction getCapability(@Nonnull Faction faction) {
        Capability<IPlayerFaction> cap = PLAYER_FACTIONS.containsKey(faction) ?
                PLAYER_FACTIONS.get(faction).PLAYER_FACTION : null;
        ServerLevel overworld = ServerLifecycleHooks.getCurrentServer().overworld();
        return Objects.nonNull(cap) ? overworld.getCapability(cap).orElse(null) : null;
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
                if(!faction.canPlayerJoin(player)) return false;
                IPlayerFaction playerFaction = getCapability(faction);
                if(Objects.nonNull(playerFaction)) pass = playerFaction.addPlayer(player);
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
