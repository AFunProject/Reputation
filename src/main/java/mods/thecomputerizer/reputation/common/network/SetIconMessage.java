package mods.thecomputerizer.reputation.common.network;

import mods.thecomputerizer.reputation.client.RenderIcon;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetIconMessage {

    private Boolean encoded;

    public SetIconMessage(FriendlyByteBuf buf){
        this.encoded = buf.readBoolean();
    }

    public SetIconMessage(boolean plus) {
        this.encoded = plus;
    }

    public static void encode(SetIconMessage message, FriendlyByteBuf buf) {
        buf.writeBoolean(message.encoded);
    }

    public static void handle(SetIconMessage message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() ->  DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> RenderIcon.setIcon(message.setIconLocation())));
        ctx.setPacketHandled(true);
    }

    private ResourceLocation setIconLocation() {
        return this.encoded ? ModDefinitions.getResource("icons/plus.png") : ModDefinitions.getResource("icons/minus.png");
    }
}
