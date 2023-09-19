package mods.thecomputerizer.reputation.network;

import mods.thecomputerizer.reputation.client.ClientTrackers;
import mods.thecomputerizer.reputation.common.ai.ChatTracker;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;

public class PacketChatIcon extends MessageImpl {

    private final List<ChatTracker> trackers;

    public PacketChatIcon(FriendlyByteBuf buf) {
        this.trackers = NetworkUtil.readGenericList(buf,ChatTracker::decode);
    }

    public PacketChatIcon(List<ChatTracker> trackers) {
        this.trackers = trackers;
    }

    @Override
    public Dist getSide() {
        return Dist.CLIENT;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        NetworkUtil.writeGenericList(buf,this.trackers,(buf1, tracker) -> tracker.encode(buf1));
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {
        ClientTrackers.setIcons(this.trackers);
    }
}
