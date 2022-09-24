package mods.thecomputerizer.reputation.common.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.client.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import javax.json.Json;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class SyncFactionPlayersMessage {

    private final ResourceLocation faction;
    private final List<UUID> uuids;
    private final int uuidSize;

    public SyncFactionPlayersMessage(FriendlyByteBuf buf) {
        faction = new ResourceLocation(buf.readUtf());
        uuidSize = buf.readInt();
        uuids = new ArrayList<>();
        for(int i=0;i<uuidSize;i++) {
            uuids.add(buf.readUUID());
        }
    }

    public SyncFactionPlayersMessage(Faction faction, List<UUID> uuids) {
        this.faction = faction.getID();
        this.uuids = uuids;
        this.uuidSize = uuids.size();
    }

    public static void encode(SyncFactionPlayersMessage message, FriendlyByteBuf buf) {
        if (message.faction!=null) buf.writeUtf(message.faction.toString());
        if (message.uuids!=null) {
            buf.writeInt(message.uuidSize);
            for(UUID uuid : message.uuids) {
                buf.writeUUID(uuid);
            }
        }
    }

    public static void handle(SyncFactionPlayersMessage message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() ->  DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.readReputationPlayersMessage(message)));
        ctx.setPacketHandled(true);
    }

    public Faction getFaction() {
        return ReputationHandler.getFaction(this.faction);
    }

    public List<UUID> getPlayerUUIDS() {
        return this.uuids;
    }
}
