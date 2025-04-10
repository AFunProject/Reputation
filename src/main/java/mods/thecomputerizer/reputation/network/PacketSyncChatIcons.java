package mods.thecomputerizer.reputation.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.reputation.client.ClientTrackers;
import mods.thecomputerizer.reputation.common.ai.ServerTrackers.Data;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.ArrayList;
import java.util.List;

public class PacketSyncChatIcons extends MessageAPI<Context> {

    private final List<Data> serverData = new ArrayList<>();

    public PacketSyncChatIcons(List<Data> serverData) {
        this.serverData.addAll(serverData);
    }
    
    public PacketSyncChatIcons(ByteBuf buf) {
        ClientTrackers.onSync(buf);
    }

    @Override public void encode(ByteBuf buf) {
        NetworkHelper.writeList(buf,this.serverData,data -> data.encode(buf));
    }

    @Override public MessageAPI<Context> handle(Context ctx) {
        return null;
    }
}