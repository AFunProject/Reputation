package mods.thecomputerizer.reputation.capability.playerfaction;

import mods.thecomputerizer.reputation.capability.Faction;
import mods.thecomputerizer.reputation.network.PacketSyncFactionPlayers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerFaction implements IPlayerFaction {

    private final Faction faction;
    private final List<UUID> playerUUIDs;

    public PlayerFaction(Faction faction) {
        this.faction = faction;
        this.playerUUIDs = new ArrayList<>();
    }

    @Override
    public Faction getFaction() {
        return this.faction;
    }

    @Override
    public boolean isPlayerAttached(Player player) {
        return this.playerUUIDs.contains(player.getUUID());
    }

    @Override
    public boolean addPlayer(Player p) {
        if(!this.isPlayerAttached(p)) this.playerUUIDs.add(p.getUUID());
        else return false;
        if(p instanceof ServerPlayer player)
            new PacketSyncFactionPlayers(this.faction.getID(),this.playerUUIDs).addPlayers(player).send();
        return true;
    }

    @Override
    public boolean removePlayer(Player p) {
        if(this.isPlayerAttached(p)) this.playerUUIDs.remove(p.getUUID());
        else return false;
        if(p instanceof ServerPlayer player)
            new PacketSyncFactionPlayers(this.faction.getID(),this.playerUUIDs).addPlayers(player).send();
        return true;
    }

    @Override
    public CompoundTag writeTag(CompoundTag tag) {
        tag.putInt("player:size",this.playerUUIDs.size());
        int i=0;
        for(UUID uuid : this.playerUUIDs) {
            tag.putUUID("player:"+i,uuid);
            i++;
        }
        return tag;
    }

    @Override
    public void readTag(CompoundTag tag) {
        if(tag.contains("player:size"))
            for(int i=0;i<tag.getInt("player:size");i++)
                this.playerUUIDs.add(tag.getUUID("player:"+i));
    }
}
