package mods.thecomputerizer.reputation.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.reputation.client.ClientTrackers;
import mods.thecomputerizer.reputation.common.ai.ChatTracker;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.List;

public class PacketChatIcon extends MessageAPI<Context> {

    private final List<ChatTracker> trackers;

    public PacketChatIcon(List<ChatTracker> trackers) {
        this.trackers = trackers;
    }
    
    public PacketChatIcon(ByteBuf buf) {
        this.trackers = NetworkHelper.readList(buf,() -> ChatTracker.decode(buf));
    }

    @Override public void encode(ByteBuf buf) {
        NetworkHelper.writeList(buf,this.trackers,tracker -> tracker.encode(buf));
    }

    @Override public MessageAPI<Context> handle(Context ctx) {
        ClientTrackers.setIcons(this.trackers);
        return null;
    }
}