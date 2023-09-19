package mods.thecomputerizer.reputation.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.capability.Faction;
import mods.thecomputerizer.reputation.capability.handlers.ReputationHandler;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PacketSyncFactions extends MessageImpl {

	private final Map<String, Integer> factionJsons;
	private final JsonElement reputationStandingsData;

	public PacketSyncFactions(FriendlyByteBuf buf) {
		this.reputationStandingsData = JsonParser.parseString(NetworkUtil.readString(buf));
		this.factionJsons = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,FriendlyByteBuf::readInt);
	}

	public PacketSyncFactions(Map<Faction, Integer> factions, JsonElement reputationStandingsData) {
		this.factionJsons = new HashMap<>();
		for(Faction f : factions.keySet()) this.factionJsons.put(f.toJsonString(), factions.get(f));
		this.reputationStandingsData = reputationStandingsData;
		Reputation.logInfo("Syncing {} factions to the client",factions.keySet().size());
	}

	@Override
	public Dist getSide() {
		return Dist.CLIENT;
	}

	public void encode(FriendlyByteBuf buf) {
		NetworkUtil.writeString(buf,this.reputationStandingsData.toString());
		NetworkUtil.writeGenericMap(buf,this.factionJsons,NetworkUtil::writeString,FriendlyByteBuf::writeInt);
	}

	@Override
	public void handle(NetworkEvent.Context ctx) {
		ReputationHandler.readPacketData(this.getFactionData(), this.getReputationStandingsData());
	}

	public HashMap<Faction, Integer> getFactionData() {
		HashMap<Faction, Integer> ret = new HashMap<>();
		for(String factionData : this.factionJsons.keySet())
			if(Objects.nonNull(factionData) && !factionData.isBlank())
				ret.put(Faction.fromJsonAsString("from server packet "+hashCode(),factionData),this.factionJsons.get(factionData));
		return ret;
	}

	public JsonElement getReputationStandingsData() {
		return this.reputationStandingsData;
	}
}
