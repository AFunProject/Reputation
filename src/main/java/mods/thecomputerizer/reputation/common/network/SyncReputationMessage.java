package mods.thecomputerizer.reputation.common.network;

import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.client.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncReputationMessage {

	private final ResourceLocation faction;
	private final int reputation;
	public SyncReputationMessage(FriendlyByteBuf buf){
		faction = new ResourceLocation(buf.readUtf());
		reputation = buf.readInt();
	}

	public SyncReputationMessage(Faction faction, int reputation) {
		this.faction = faction.getID();
		this.reputation = reputation;
	}

	public static void encode(SyncReputationMessage message, FriendlyByteBuf buf) {
		if (message.faction!=null) buf.writeUtf(message.faction.toString());
		buf.writeInt(message.reputation);
	}

	public static void handle(SyncReputationMessage message, Supplier<NetworkEvent.Context> context) {
		NetworkEvent.Context ctx = context.get();
		ctx.enqueueWork(() -> {});
		ClientHandler.readReputationMessage(message);
		ctx.setPacketHandled(true);
	}

	public Faction getFaction() {
		return ReputationHandler.getFaction(faction);
	}

	public int getReputation() {
		return reputation;
	}

}
