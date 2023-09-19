package mods.thecomputerizer.reputation.network;

import mods.thecomputerizer.reputation.client.ClientTrackers;
import mods.thecomputerizer.reputation.common.ai.ServerTrackers;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;

public class PacketSyncChatIcons extends MessageImpl {

    private final List<ServerTrackers.Data> serverData = new ArrayList<>();

    public PacketSyncChatIcons(FriendlyByteBuf buf) {
        ClientTrackers.onSync(buf);
    }

    public PacketSyncChatIcons(List<ServerTrackers.Data> serverData) {
        this.serverData.addAll(serverData);
    }

    @Override
    public Dist getSide() {
        return Dist.CLIENT;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        NetworkUtil.writeGenericList(buf,this.serverData,(buf1, data) -> data.encode(buf1));
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {}
}
