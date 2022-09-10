package mods.thecomputerizer.reputation.common.network;

import mods.thecomputerizer.reputation.client.ClientHandler;
import mods.thecomputerizer.reputation.client.event.RenderEvents;
import mods.thecomputerizer.reputation.client.render.RenderIcon;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import mods.thecomputerizer.reputation.common.registration.Sounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetIconMessage {
    private final Boolean encoded;
    private final ResourceLocation factionIcon;
    private final ResourceLocation faction;
    private final int amount;


    public SetIconMessage(FriendlyByteBuf buf){
        this.encoded = buf.readBoolean();
        this.factionIcon = buf.readResourceLocation();
        this.faction = buf.readResourceLocation();
        this.amount = buf.readInt();
    }

    public SetIconMessage(boolean plus, ResourceLocation faction, int amount) {
        this.encoded = plus;
        this.factionIcon = new ResourceLocation(ModDefinitions.MODID,"textures/icons/faction_"+faction.getPath()+".png");
        this.faction = new ResourceLocation(ModDefinitions.MODID, faction.getPath());
        this.amount = amount;
    }

    public static void encode(SetIconMessage message, FriendlyByteBuf buf) {
        buf.writeBoolean(message.encoded);
        buf.writeResourceLocation(message.factionIcon);
        buf.writeResourceLocation(message.faction);
        buf.writeInt(message.amount);
    }

    public static void handle(SetIconMessage message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() ->  {});
        RenderEvents.CLIENT_FACTIONS_REPUTATION.put(RenderEvents.CLIENT_FACTIONS.get(message.faction),
                RenderEvents.CLIENT_FACTIONS_REPUTATION.get(RenderEvents.CLIENT_FACTIONS.get(message.faction))+message.amount);
        RenderIcon.setIcon(message.setIconLocation(),message.factionIcon);
        ClientHandler.playPacketSound(message.encoded ? Sounds.INCREASE_REPUTATION.get() : Sounds.DECREASE_REPUTATION.get());
        ctx.setPacketHandled(true);
    }

    private ResourceLocation setIconLocation() {
        return this.encoded ? ModDefinitions.getResource("textures/icons/reputation_increase.png") : ModDefinitions.getResource("textures/icons/reputation_decrease.png");
    }
}
