package mods.thecomputerizer.reputation.network;

import mods.thecomputerizer.reputation.client.ClientHandler;
import mods.thecomputerizer.reputation.client.ClientEvents;
import mods.thecomputerizer.reputation.registry.SoundRegistry;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class PacketFleeIcon extends MessageImpl {
    private final UUID uuid;
    private final boolean add;

    public PacketFleeIcon(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.add = buf.readBoolean();
    }

    public PacketFleeIcon(UUID uuid, boolean add) {
        this.uuid = uuid;
        this.add = add;
    }

    @Override
    public Dist getSide() {
        return Dist.CLIENT;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(this.uuid);
        buf.writeBoolean(this.add);
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {
        if(!ClientEvents.FLEEING_MOBS.contains(this.uuid) && this.add) {
            ClientEvents.FLEEING_MOBS.add(this.uuid);
            ClientHandler.playPacketSound(SoundRegistry.FLEE.get());
        }
        else if(!this.add) ClientEvents.FLEEING_MOBS.remove(this.uuid);
    }
}
