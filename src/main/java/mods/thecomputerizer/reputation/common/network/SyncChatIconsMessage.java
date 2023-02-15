package mods.thecomputerizer.reputation.common.network;

import mods.thecomputerizer.reputation.client.ClientTrackers;
import mods.thecomputerizer.reputation.common.ai.ServerTrackers;
import mods.thecomputerizer.reputation.util.NetworkUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncChatIconsMessage {

    public SyncChatIconsMessage(FriendlyByteBuf buf) {
        ClientTrackers.onSync(buf);
    }

    private final List<ServerTrackers.Data> serverData = new ArrayList<>();

    public SyncChatIconsMessage(List<ServerTrackers.Data> serverData) {
        this.serverData.addAll(serverData);
    }

    public static void encode(SyncChatIconsMessage message, FriendlyByteBuf buf) {
        NetworkUtil.writeGenericList(buf,message.serverData,(buf1, data) -> data.encode(buf1));
    }

    public static void handle(SyncChatIconsMessage ignoredMessage, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() ->  {});
        ctx.setPacketHandled(true);
    }
}
