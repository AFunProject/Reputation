package mods.thecomputerizer.reputation.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.reputation.client.ClientHandler;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.UUID;

import static mods.thecomputerizer.reputation.client.ClientEvents.FLEEING_MOBS;
import static mods.thecomputerizer.reputation.registry.SoundRegistry.FLEE;

public class PacketFleeIcon extends MessageAPI<Context> {
    
    private final UUID uuid;
    private final boolean add;

    public PacketFleeIcon(UUID uuid, boolean add) {
        this.uuid = uuid;
        this.add = add;
    }
    
    public PacketFleeIcon(ByteBuf buf) {
        this.uuid = ReputationNetwork.readUUID(buf);
        this.add = buf.readBoolean();
    }

    @Override public void encode(ByteBuf buf) {
        ReputationNetwork.writeUUID(buf,this.uuid);
        buf.writeBoolean(this.add);
    }

    @Override public MessageAPI<Context> handle(Context ctx) {
        if(!FLEEING_MOBS.contains(this.uuid) && this.add) {
            FLEEING_MOBS.add(this.uuid);
            ClientHandler.playPacketSound(FLEE.get());
        } else if(!this.add) FLEEING_MOBS.remove(this.uuid);
        return null;
    }
}