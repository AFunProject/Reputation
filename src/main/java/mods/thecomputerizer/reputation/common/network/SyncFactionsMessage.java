package mods.thecomputerizer.reputation.common.network;

import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class SyncFactionsMessage {

	private final String data;

	public SyncFactionsMessage(FriendlyByteBuf buf){
		data = buf.readUtf();
	}

	public SyncFactionsMessage(Collection<Faction> factions) {
		StringBuilder builder = new StringBuilder();
		for (Faction faction : factions) {
			builder.append(faction.toJsonString());
			builder.append(";");
		}
		data = builder.toString();
	}

	public void encode(FriendlyByteBuf buf) {
		if (data!=null) buf.writeUtf(data);
	}

	public static void handle(SyncFactionsMessage message, Supplier<NetworkEvent.Context> context) {
		NetworkEvent.Context ctx = context.get();
		ctx.enqueueWork(() -> {});
		ReputationHandler.readPacketData(message.getFactionData());
		ctx.setPacketHandled(true);
	}

	public Collection<Faction> getFactionData() {
		Set<Faction> factions = new HashSet<>();
		for (String factionData : data.split(";")) {
			if (factionData!=null && !factionData.isBlank()) {
				factions.add(Faction.fromJsonAsString("from server packet " + hashCode(), factionData));
			}
		}
		return factions;
	}

}
