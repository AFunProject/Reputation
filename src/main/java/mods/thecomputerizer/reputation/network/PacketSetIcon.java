package mods.thecomputerizer.reputation.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.reputation.ReputationRef;
import mods.thecomputerizer.reputation.client.ClientHandler;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.wrappers.WrapperHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.HashMap;
import java.util.Map;

import static mods.thecomputerizer.reputation.client.ClientEvents.CLIENT_FACTIONS;
import static mods.thecomputerizer.reputation.client.ClientEvents.CLIENT_FACTIONS_REPUTATION;
import static mods.thecomputerizer.reputation.registry.SoundRegistry.DECREASE_REPUTATION;
import static mods.thecomputerizer.reputation.registry.SoundRegistry.INCREASE_REPUTATION;

public class PacketSetIcon extends MessageAPI<Context> {

    private static final float ICON_SCALE = 0.32f;
    private final Boolean encoded;
    private final ResourceLocation factionIcon;
    private final ResourceLocation faction;
    private final int amount;

    public PacketSetIcon(boolean plus, ResourceLocation faction, int amount) {
        this.encoded = plus;
        this.factionIcon = ReputationRef.res("textures/icons/faction_"+faction.getPath()+".png");
        this.faction = ReputationRef.res(faction.getPath());
        this.amount = amount;
    }
    
    public PacketSetIcon(ByteBuf buf) {
        this.encoded = buf.readBoolean();
        this.factionIcon = ReputationNetwork.readResourceLocation(buf);
        this.faction = ReputationNetwork.readResourceLocation(buf);
        this.amount = buf.readInt();
    }
    
    private void addPNG(ResourceLocation location, Map<String,Object> args) {
        RenderHelper.addRenderable(RenderHelper.initPNG(WrapperHelper.wrapResourceLocation(location),args));
    }

    @Override public void encode(ByteBuf buf) {
        buf.writeBoolean(this.encoded);
        ReputationNetwork.writeResourceLocation(buf,this.factionIcon);
        ReputationNetwork.writeResourceLocation(buf,this.faction);
        buf.writeInt(this.amount);
    }

    @Override public MessageAPI<Context> handle(Context ctx) {
        CLIENT_FACTIONS_REPUTATION.put(CLIENT_FACTIONS.get(this.faction),
                CLIENT_FACTIONS_REPUTATION.get(CLIENT_FACTIONS.get(this.faction))+this.amount);
        addPNG(this.setIconLocation(),makeVarMap(true,getIconWidth(true)));
        addPNG(this.factionIcon,makeVarMap(false,getIconWidth(false)));
        ClientHandler.playPacketSound(this.encoded ? INCREASE_REPUTATION.get() : DECREASE_REPUTATION.get());
        return null;
    }

    private Map<String,Object> makeVarMap(boolean offset, float iconWidth) {
        float scale = offset ? ICON_SCALE*0.75f : ICON_SCALE;
        Map<String,Object> ret = new HashMap<>();
        ret.put("time",150L);
        ret.put("fade_out",35L);
        ret.put("scale_x",scale);
        ret.put("scale_y",scale);
        ret.put("x",offset ? (int)(1.75f*(1/scale)*iconWidth) : (int)(0.5f*(1/scale)*iconWidth));
        ret.put("horizontal_alignment","right");
        return ret;
    }

    private float getIconWidth(boolean offset) {
        float scale = -1f*(offset ? ICON_SCALE*0.75f : ICON_SCALE);
        return (float)Minecraft.getInstance().getWindow().getGuiScaledWidth()*scale;
    }

    private ResourceLocation setIconLocation() {
        return this.encoded ? ReputationRef.res("textures/icons/reputation_increase.png") :
                ReputationRef.res("textures/icons/reputation_decrease.png");
    }
}