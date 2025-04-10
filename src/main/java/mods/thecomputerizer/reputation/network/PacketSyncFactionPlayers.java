package mods.thecomputerizer.reputation.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.reputation.capability.Faction;
import mods.thecomputerizer.reputation.capability.handlers.ReputationHandler;
import mods.thecomputerizer.reputation.client.ClientHandler;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PacketSyncFactionPlayers extends MessageAPI<Context> {

    private final ResourceLocation factionID;
    private final List<UUID> uuids;

    public PacketSyncFactionPlayers(ResourceLocation factionID, List<UUID> uuids) {
        this.factionID = factionID;
        this.uuids = uuids;
    }
    
    public PacketSyncFactionPlayers(ByteBuf buf) {
        this.factionID = ReputationNetwork.readResourceLocation(buf);
        this.uuids = NetworkHelper.readList(buf,() -> ReputationNetwork.readUUID(buf));
    }

    @Override public void encode(ByteBuf buf) {
        ReputationNetwork.writeResourceLocation(buf,this.factionID);
        NetworkHelper.writeList(buf,this.uuids,uuid -> ReputationNetwork.writeUUID(buf,uuid));
    }

    @Override public MessageAPI<Context> handle(Context ctx) {
        if(Objects.nonNull(this.factionID) && !this.uuids.isEmpty()) {
            Faction faction = ReputationHandler.getFaction(this.factionID);
            if(Objects.nonNull(faction)) ClientHandler.readReputationPlayersMessage(faction,this.uuids);
        }
        return null;
    }
}