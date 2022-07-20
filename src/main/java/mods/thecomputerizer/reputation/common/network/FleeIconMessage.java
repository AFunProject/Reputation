package mods.thecomputerizer.reputation.common.network;

import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.client.event.RenderEvents;
import mods.thecomputerizer.reputation.common.registration.Sounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
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
    }

    public static void encode(FleeIconMessage message, FriendlyByteBuf buf) {
        buf.writeUUID(message.uuid);
        buf.writeBoolean(message.add);
    }

    public static void handle(FleeIconMessage message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() ->  {});
        if(!RenderEvents.fleeingMobs.contains(message.uuid) && message.add) {
            RenderEvents.fleeingMobs.add(message.uuid);
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(Sounds.FLEE.get(), Mth.randomBetween(ReputationHandler.random,0.88f,1.12f)));
        }
        else if(!message.add) RenderEvents.fleeingMobs.remove(message.uuid);
        ctx.setPacketHandled(true);
    }
}
