package mods.thecomputerizer.reputation.common.network;

import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.function.Supplier;

public class SyncFactionsMessage {

	private final HashMap<String, Integer> factionJsons;

	public SyncFactionsMessage(FriendlyByteBuf buf){
		this.factionJsons = new HashMap<>();
		int size = buf.readInt();
		for(int i=0;i<size;i++) this.factionJsons.put((String)buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8),buf.readInt());
	}

	public SyncFactionsMessage(HashMap<Faction, Integer> factions) {
		this.factionJsons = new HashMap<>();
		for(Faction f : factions.keySet()) this.factionJsons.put(f.toJsonString(), factions.get(f));
	}

	public void encode(FriendlyByteBuf buf) {
		if (!this.factionJsons.keySet().isEmpty()) {
			buf.writeInt(this.factionJsons.keySet().size());
			for(String jsonString : this.factionJsons.keySet()) {
				buf.writeInt(jsonString.length());
				buf.writeCharSequence(jsonString, StandardCharsets.UTF_8);
				buf.writeInt(this.factionJsons.get(jsonString));
			}
		}
	}

	public static void handle(SyncFactionsMessage message, Supplier<NetworkEvent.Context> context) {
		NetworkEvent.Context ctx = context.get();
		ctx.enqueueWork(() -> {});
		ReputationHandler.readPacketData(message.getFactionData());
		ctx.setPacketHandled(true);
	}

	public HashMap<Faction, Integer> getFactionData() {
		HashMap<Faction, Integer> ret = new HashMap<>();
		for (String factionData : this.factionJsons.keySet())
			if (factionData!=null && !factionData.isBlank())
				ret.put(Faction.fromJsonAsString("from server packet " + hashCode(), factionData),this.factionJsons.get(factionData));
		return ret;
	}

}
