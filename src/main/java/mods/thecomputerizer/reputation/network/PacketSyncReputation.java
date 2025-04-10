package mods.thecomputerizer.reputation.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.reputation.capability.Faction;
import mods.thecomputerizer.reputation.capability.handlers.ReputationHandler;
import mods.thecomputerizer.reputation.client.ClientHandler;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.Objects;

public class PacketSyncReputation extends MessageAPI<Context> {

	private final ResourceLocation factionID;
	private final int reputation;
	
	public PacketSyncReputation(ByteBuf buf) {
		this.factionID = ReputationNetwork.readResourceLocation(buf);
		this.reputation = buf.readInt();
	}

	public PacketSyncReputation(ResourceLocation factionID, int reputation) {
		this.factionID = factionID;
		this.reputation = reputation;
	}

	@Override public void encode(ByteBuf buf) {
		ReputationNetwork.writeResourceLocation(buf,this.factionID);
		buf.writeInt(this.reputation);
	}

	@Override public MessageAPI<Context> handle(Context ctx) {
		if(Objects.nonNull(this.factionID)) {
			Faction faction = ReputationHandler.getFaction(this.factionID);
			if(Objects.nonNull(faction)) ClientHandler.readReputationMessage(faction,this.reputation);
		}
		return null;
	}
}