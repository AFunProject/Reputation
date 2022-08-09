package mods.thecomputerizer.reputation.common.network;

import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.client.ClientTrackers;
import mods.thecomputerizer.reputation.common.ai.ChatTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ChatIconMessage {

    private final List<ChatTracker> trackers = new ArrayList<>();

    public ChatIconMessage(FriendlyByteBuf buf){
        int k = buf.readInt();
        for(int i = 0;i<k;i++) trackers.add(ChatTracker.decode(buf));
    }

    public ChatIconMessage(List<ChatTracker> trackers) {
        Reputation.logInfo("SYNCING "+trackers.size()+" CHAT ICON TRACKERS TO THE CLIENT");
        this.trackers.addAll(trackers);
    }

    public static void encode(ChatIconMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.trackers.size());
        for(ChatTracker tracker : message.trackers) tracker.encode(buf);
    }

    public static void handle(ChatIconMessage message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() ->  {});
        for(ChatTracker tracker: message.trackers) ClientTrackers.initTracker(tracker);
        ctx.setPacketHandled(true);
    }
}
