package mods.thecomputerizer.reputation.common.network;

import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.client.event.RenderEvents;
import mods.thecomputerizer.reputation.client.render.RenderIcon;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class FleeIconMessage {
    private final UUID uuid;
    private final boolean add;

    public FleeIconMessage(FriendlyByteBuf buf){
        this.uuid = buf.readUUID();
        this.add = buf.readBoolean();
    }

    public FleeIconMessage(UUID uuid, boolean add) {
        this.uuid = uuid;
        this.add = add;
        Reputation.logInfo("Calling flee packet (performance check)");
    }

    public static void encode(FleeIconMessage message, FriendlyByteBuf buf) {
        buf.writeUUID(message.uuid);
        buf.writeBoolean(message.add);
    }

    public static void handle(FleeIconMessage message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() ->  {});
        if(!RenderEvents.fleeingMobs.contains(message.uuid) && message.add) RenderEvents.fleeingMobs.add(message.uuid);
        else if(!message.add) RenderEvents.fleeingMobs.remove(message.uuid);
        ctx.setPacketHandled(true);
    }
}
