package mods.thecomputerizer.reputation.network;

import mods.thecomputerizer.reputation.capability.Faction;
import mods.thecomputerizer.reputation.capability.handlers.ReputationHandler;
import mods.thecomputerizer.reputation.client.ClientHandler;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;

public class PacketSyncReputation extends MessageImpl {

	private final ResourceLocation factionID;
	private final int reputation;
	public PacketSyncReputation(FriendlyByteBuf buf) {
		this.factionID = buf.readResourceLocation();
		this.reputation = buf.readInt();
	}

	public PacketSyncReputation(ResourceLocation factionID, int reputation) {
		this.factionID = factionID;
		this.reputation = reputation;
	}

	@Override
	public Dist getSide() {
		return Dist.CLIENT;
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeResourceLocation(this.factionID);
		buf.writeInt(this.reputation);
	}

	@Override
	public void handle(NetworkEvent.Context ctx) {
		if(Objects.nonNull(this.factionID)) {
			Faction faction = ReputationHandler.getFaction(this.factionID);
			if(Objects.nonNull(faction)) ClientHandler.readReputationMessage(faction,this.reputation);
		}
	}
}
