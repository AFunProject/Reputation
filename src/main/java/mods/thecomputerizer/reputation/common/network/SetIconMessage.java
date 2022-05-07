package mods.thecomputerizer.reputation.common.network;

import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.client.render.RenderIcon;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import mods.thecomputerizer.reputation.common.registration.Sounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetIconMessage {
    private final Boolean encoded;
    private final ResourceLocation faction;


    public SetIconMessage(FriendlyByteBuf buf){
        this.encoded = buf.readBoolean();
        this.faction = buf.readResourceLocation();
    }

    public SetIconMessage(boolean plus, ResourceLocation faction) {
        this.encoded = plus;
        this.faction = new ResourceLocation(ModDefinitions.MODID,"textures/icons/faction_"+faction.getPath()+".png");
    }

    public static void encode(SetIconMessage message, FriendlyByteBuf buf) {
        buf.writeBoolean(message.encoded);
        buf.writeResourceLocation(message.faction);
    }

    public static void handle(SetIconMessage message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() ->  {});
        RenderIcon.setIcon(message.setIconLocation(),message.faction);
        SoundEvent repSound = message.encoded ? Sounds.INCREASE_REPUTATION.get() : Sounds.DECREASE_REPUTATION.get();
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(repSound, Mth.randomBetween(ReputationHandler.random,0.85f,1.15f)));
        ctx.setPacketHandled(true);
    }

    private ResourceLocation setIconLocation() {
        return this.encoded ? ModDefinitions.getResource("textures/icons/reputation_increase.png") : ModDefinitions.getResource("textures/icons/reputation_decrease.png");
    }
}
