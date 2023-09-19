package mods.thecomputerizer.reputation.network;

import mods.thecomputerizer.reputation.capability.Faction;
import mods.thecomputerizer.reputation.capability.handlers.ReputationHandler;
import mods.thecomputerizer.reputation.client.ClientHandler;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PacketSyncFactionPlayers extends MessageImpl {

    private final ResourceLocation factionID;
    private final List<UUID> uuids;

    public PacketSyncFactionPlayers(FriendlyByteBuf buf) {
        this.factionID = buf.readResourceLocation();
        this.uuids = NetworkUtil.readGenericList(buf,FriendlyByteBuf::readUUID);
    }

    public PacketSyncFactionPlayers(ResourceLocation factionID, List<UUID> uuids) {
        this.factionID = factionID;
        this.uuids = uuids;
    }

    @Override
    public Dist getSide() {
        return Dist.CLIENT;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.factionID);
        NetworkUtil.writeGenericList(buf,this.uuids,FriendlyByteBuf::writeUUID);
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {
        if(Objects.nonNull(this.factionID) && !this.uuids.isEmpty()) {
            Faction faction = ReputationHandler.getFaction(this.factionID);
            if(Objects.nonNull(faction)) ClientHandler.readReputationPlayersMessage(faction, this.uuids);
        }
    }
}
