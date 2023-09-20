package mods.thecomputerizer.reputation.network;

import mods.thecomputerizer.reputation.Constants;
import mods.thecomputerizer.reputation.client.ClientEvents;
import mods.thecomputerizer.reputation.client.ClientHandler;
import mods.thecomputerizer.reputation.registry.SoundRegistry;
import mods.thecomputerizer.theimpossiblelibrary.client.render.Renderer;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;

public class PacketSetIcon extends MessageImpl {

    private static final float ICON_SCALE = 0.32f;
    private final Boolean encoded;
    private final ResourceLocation factionIcon;
    private final ResourceLocation faction;
    private final int amount;


    public PacketSetIcon(FriendlyByteBuf buf) {
        this.encoded = buf.readBoolean();
        this.factionIcon = buf.readResourceLocation();
        this.faction = buf.readResourceLocation();
        this.amount = buf.readInt();
    }

    public PacketSetIcon(boolean plus, ResourceLocation faction, int amount) {
        this.encoded = plus;
        this.factionIcon = Constants.res("textures/icons/faction_"+faction.getPath()+".png");
        this.faction = Constants.res(faction.getPath());
        this.amount = amount;
    }

    @Override
    public Dist getSide() {
        return Dist.CLIENT;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.encoded);
        buf.writeResourceLocation(this.factionIcon);
        buf.writeResourceLocation(this.faction);
        buf.writeInt(this.amount);
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {
        ClientEvents.CLIENT_FACTIONS_REPUTATION.put(ClientEvents.CLIENT_FACTIONS.get(this.faction),
                ClientEvents.CLIENT_FACTIONS_REPUTATION.get(ClientEvents.CLIENT_FACTIONS.get(this.faction))+this.amount);
        Renderer.addRenderable(Renderer.initializePng(this.setIconLocation(),makeVarMap(true,getIconWidth(true))));
        Renderer.addRenderable(Renderer.initializePng(this.factionIcon,makeVarMap(false,getIconWidth(false))));
        ClientHandler.playPacketSound(this.encoded ? SoundRegistry.INCREASE_REPUTATION.get() : SoundRegistry.DECREASE_REPUTATION.get());
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
        return this.encoded ? Constants.res("textures/icons/reputation_increase.png") :
                Constants.res("textures/icons/reputation_decrease.png");
    }
}
