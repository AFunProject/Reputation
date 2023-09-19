package mods.thecomputerizer.reputation.client;

import mods.thecomputerizer.reputation.Constants;
import mods.thecomputerizer.reputation.capability.Faction;
import mods.thecomputerizer.reputation.capability.handlers.PlayerFactionHandler;
import mods.thecomputerizer.reputation.capability.handlers.ReputationHandler;
import mods.thecomputerizer.reputation.capability.playerfaction.IPlayerFaction;
import mods.thecomputerizer.reputation.capability.playerfaction.PlayerFactionProvider;
import mods.thecomputerizer.reputation.capability.reputation.IReputation;
import mods.thecomputerizer.reputation.common.ai.ReputationStandings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ClientHandler {
	public static ReputationStandings standings;

	public static void readReputationMessage(Faction faction, int reputation) {
		Minecraft mc = Minecraft.getInstance();
		if(Objects.nonNull(mc.player)) {
			IReputation cap = ReputationHandler.getCapability(mc.player);
			if(Objects.nonNull(cap)) cap.setReputation(mc.player,faction,reputation);
		}
	}

	@SuppressWarnings("DataFlowIssue")
	public static void readReputationPlayersMessage(Faction faction, List<UUID> uuids) {
		Minecraft mc = Minecraft.getInstance();
		Level overworld = ServerLifecycleHooks.getCurrentServer().overworld();
		if(Objects.nonNull(mc.player) && Objects.nonNull(mc.level)) {
			PlayerFactionProvider provider = PlayerFactionHandler.PLAYER_FACTIONS.get(faction);
			Capability<IPlayerFaction> cap = Objects.nonNull(provider) ? provider.PLAYER_FACTION : null;
			IPlayerFaction playerFaction = Objects.nonNull(cap) ? overworld.getCapability(cap).orElse(null) : null;
			if (Objects.nonNull(playerFaction)) {
				for(UUID uuid : uuids) {
					Player player = mc.level.getPlayerByUUID(uuid);
					if(Objects.nonNull(player)) playerFaction.addPlayer(player);
				}
			}
		}
	}

	public static void playPacketSound(SoundEvent sound) {
		SoundManager manager = Minecraft.getInstance().getSoundManager();
		manager.play(SimpleSoundInstance.forUI(sound, Constants.floatRand(0.88f,1.12f)));
	}
}
