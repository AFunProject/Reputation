package mods.thecomputerizer.reputation.common.network;

import mods.thecomputerizer.reputation.client.ClientTrackers;
import mods.thecomputerizer.reputation.common.ai.ChatTracker;
import mods.thecomputerizer.reputation.util.NetworkUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class ChatIconMessage {

    private List<ChatTracker> trackers;

    public ChatIconMessage(FriendlyByteBuf buf) {
        ClientTrackers.setIcons(NetworkUtil.readGenericList(buf,ChatTracker::decode));
    }

    public ChatIconMessage(List<ChatTracker> trackers) {
        this.trackers = trackers;
    }

    public static void encode(ChatIconMessage message, FriendlyByteBuf buf) {
        NetworkUtil.writeGenericList(buf,message.trackers,(buf1, tracker) -> tracker.encode(buf1));
    }

    @SuppressWarnings("unused")
    public static void handle(ChatIconMessage message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() ->  {});
        ctx.setPacketHandled(true);
    }
}
