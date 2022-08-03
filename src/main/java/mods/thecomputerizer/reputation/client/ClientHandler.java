package mods.thecomputerizer.reputation.client;

import mods.thecomputerizer.reputation.api.PlayerFactionHandler;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.api.capability.IPlayerFaction;
import mods.thecomputerizer.reputation.api.capability.IReputation;
import mods.thecomputerizer.reputation.common.network.SyncFactionPlayersMessage;
import mods.thecomputerizer.reputation.common.network.SyncReputationMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.UUID;

@SuppressWarnings("OptionalGetWithoutIsPresent")
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

	public static void readReputationPlayersMessage(SyncFactionPlayersMessage message) {
		Minecraft mc = Minecraft.getInstance();
		Level overworld = ServerLifecycleHooks.getCurrentServer().overworld();
		Player player = mc.player;
		if(player!=null) {
			LazyOptional<IPlayerFaction> optional = overworld.getCapability(PlayerFactionHandler.PLAYER_FACTIONS.get(message.getFaction()).PLAYER_FACTION);
			if (optional.isPresent() && message.getFaction() != null) {
				IPlayerFaction cap = optional.resolve().get();
				for(UUID uuid : message.getPlayerUUIDS()) {
					assert mc.level != null;
					cap.addPlayer(mc.level.getPlayerByUUID(uuid));
				}
			}
		}
	}

	public static void playPacketSound(SoundEvent sound) {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, Mth.randomBetween(ReputationHandler.random,0.88f,1.12f)));
	}
}
