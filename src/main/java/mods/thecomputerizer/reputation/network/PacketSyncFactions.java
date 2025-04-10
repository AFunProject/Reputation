package mods.thecomputerizer.reputation.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.capability.Faction;
import mods.thecomputerizer.reputation.capability.handlers.ReputationHandler;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PacketSyncFactions extends MessageAPI<Context> {

	private final Map<String,Integer> factionJsons;
	private final JsonElement reputationStandingsData;

	public PacketSyncFactions(Map<Faction,Integer> factions, JsonElement reputationStandingsData) {
		this.factionJsons = new HashMap<>();
		for(Faction f : factions.keySet()) this.factionJsons.put(f.toJsonString(),factions.get(f));
		this.reputationStandingsData = reputationStandingsData;
		Reputation.logInfo("Syncing {} factions to the client",factions.size());
	}
	
	public PacketSyncFactions(ByteBuf buf) {
		this.reputationStandingsData = JsonParser.parseString(NetworkHelper.readString(buf));
		this.factionJsons = NetworkHelper.readMap(buf,() -> NetworkHelper.readString(buf),buf::readInt);
	}

	@Override public void encode(ByteBuf buf) {
		NetworkHelper.writeString(buf,this.reputationStandingsData.toString());
		NetworkHelper.writeMap(buf,this.factionJsons,key -> NetworkHelper.writeString(buf,key),buf::writeInt);
	}

	@Override public MessageAPI<Context> handle(Context ctx) {
		ReputationHandler.readPacketData(getFactionData(),getReputationStandingsData());
		return null;
	}

	public Map<Faction,Integer> getFactionData() {
		Map<Faction,Integer> ret = new HashMap<>();
		for(String factionData : this.factionJsons.keySet())
			if(Objects.nonNull(factionData) && !factionData.isBlank())
				ret.put(Faction.fromJsonAsString("from server packet "+hashCode(),factionData),
						this.factionJsons.get(factionData));
		return ret;
	}

	public JsonElement getReputationStandingsData() {
		return this.reputationStandingsData;
	}
}