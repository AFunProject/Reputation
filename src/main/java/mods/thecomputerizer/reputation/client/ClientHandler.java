package mods.thecomputerizer.reputation.client;

import mods.thecomputerizer.reputation.api.capability.IReputation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.common.network.SyncReputationMessage;

public class ClientHandler {

	public static void readReputationMessage(SyncReputationMessage message) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if(player!=null) {
			LazyOptional<IReputation> optional = player.getCapability(ReputationHandler.REPUTATION_CAPABILITY);
			if (optional.isPresent() && message.getFaction() != null) {
				IReputation cap = optional.resolve().get();
				cap.setReputation(player, message.getFaction(), message.getReputation());
			}
		}
	}
}
