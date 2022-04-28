package mods.thecomputerizer.reputation.common.capability;

import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.capability.IPlayerFaction;
import mods.thecomputerizer.reputation.common.network.PacketHandler;
import mods.thecomputerizer.reputation.common.network.SyncFactionPlayersMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerFaction implements IPlayerFaction {

    private final Faction faction;
    private final List<UUID> PLAYER_UUIDS;

    public PlayerFaction(Faction faction) {
        this.faction = faction;
        this.PLAYER_UUIDS = new ArrayList<>();
    }

    @Override
    public Faction getFaction() {
        return this.faction;
    }

    @Override
    public boolean isPlayerAttached(Player player) {
        return this.PLAYER_UUIDS.contains(player.getUUID());
    }

    @Override
    public void addPlayer(Player player) {
        if(!this.isPlayerAttached(player)) this.PLAYER_UUIDS.add(player.getUUID());
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER && player instanceof ServerPlayer) {
            PacketHandler.sendTo(new SyncFactionPlayersMessage(faction, PLAYER_UUIDS),(ServerPlayer)player);
        }
    }

    @Override
    public void removePlayer(Player player) {
        if(this.isPlayerAttached(player)) this.PLAYER_UUIDS.remove(player.getUUID());
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER && player instanceof ServerPlayer) {
            PacketHandler.sendTo(new SyncFactionPlayersMessage(faction, PLAYER_UUIDS),(ServerPlayer)player);
        }
    }

    @Override
    public CompoundTag writeNBT(CompoundTag nbt) {
        nbt.putInt("player:size",this.PLAYER_UUIDS.size());
        int i=0;
        for(UUID uuid : this.PLAYER_UUIDS) {
            nbt.putUUID("player:"+i,uuid);
            i++;
        }
        return nbt;
    }

    @Override
    public void readNBT(CompoundTag nbt) {
        if(nbt.contains("player:size")) {
            for(int i=0;i<nbt.getInt("player:size");i++) {
                PLAYER_UUIDS.add(nbt.getUUID("player:"+i));
            }
        }
    }
}
