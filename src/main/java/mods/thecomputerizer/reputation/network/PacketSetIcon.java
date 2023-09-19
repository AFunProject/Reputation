package mods.thecomputerizer.reputation.network;

import mods.thecomputerizer.reputation.client.ClientHandler;
import mods.thecomputerizer.reputation.client.ClientEvents;
import mods.thecomputerizer.reputation.Constants;
import mods.thecomputerizer.reputation.registry.SoundRegistry;
import mods.thecomputerizer.theimpossiblelibrary.client.render.Renderer;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;

public class PacketSetIcon extends MessageImpl {
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
        Renderer.addRenderable(Renderer.initializePng(this.setIconLocation(),makeVarMap(true)));
        Renderer.addRenderable(Renderer.initializePng(this.factionIcon,makeVarMap(false)));
        ClientHandler.playPacketSound(this.encoded ? SoundRegistry.INCREASE_REPUTATION.get() : SoundRegistry.DECREASE_REPUTATION.get());
    }

    private Map<String,Object> makeVarMap(boolean offset) {
        Map<String,Object> ret = new HashMap<>();
        ret.put("time",500L);
        ret.put("fade_out",35L);
        ret.put("scale_x",0.36f);
        ret.put("scale_y",0.36f);
        ret.put("x",offset ? -45 : -36);
        ret.put("horizontal_alignment","right");
        return ret;
    }

    private ResourceLocation setIconLocation() {
        return this.encoded ? Constants.res("textures/icons/reputation_increase.png") :
                Constants.res("textures/icons/reputation_decrease.png");
    }
}
