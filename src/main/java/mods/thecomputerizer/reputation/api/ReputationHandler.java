package mods.thecomputerizer.reputation.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mods.thecomputerizer.reputation.api.capability.IReputation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class ReputationHandler {

	public static Capability<IReputation> REPUTATION_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

	private static Map<ResourceLocation, Faction> FACTIONS = new HashMap<>();
	private static Map<ResourceLocation, Faction> CLIENT_FACTIONS = new HashMap<>();

	public static void registerFaction(Faction faction) {
		if(!faction.getName().getPath().isEmpty()) FACTIONS.put(faction.getName(), faction);
	}

	public static Faction getFaction(ResourceLocation loc) {
		return getSidedList().get(loc);
	}

	public static Collection<Faction> getFactions() {
		return getSidedList().values();
	}

	public static Collection<Faction> getEntityFactions(LivingEntity entity) {
		Set<Faction> factions = new HashSet<>();
		for (Faction faction : getFactions()) {
			if (faction.isMember(entity)) {
				factions.add(faction);
			}
		}
		return factions;
	}

	private static Map<ResourceLocation, Faction> getSidedList() {
		return Thread.currentThread().getThreadGroup() == SidedThreadGroups.CLIENT ? CLIENT_FACTIONS : FACTIONS;
	}

	public static int getReputation(Player player, Faction faction) {
		LazyOptional<IReputation> optional = player.getCapability(ReputationHandler.REPUTATION_CAPABILITY);
		if (optional.isPresent()) {
			IReputation reputation = optional.resolve().get();
			return reputation.getReputation(faction);
		}
		return 0;
	}

	public static void changeReputation(Player player, Faction faction, int amount) {
		LazyOptional<IReputation> optional = player.getCapability(ReputationHandler.REPUTATION_CAPABILITY);
		if (optional.isPresent()) {
			IReputation reputation = optional.resolve().get();
			reputation.changeReputation(player, faction, amount);
			for (Faction enemy : faction.getEnemies()) {
				reputation.changeReputation(player, enemy, -amount);
			}
		}
	}

	public static void changeReputationStrict(Player player, Faction faction, int amount) {
		LazyOptional<IReputation> optional = player.getCapability(ReputationHandler.REPUTATION_CAPABILITY);
		if (optional.isPresent()) {
			IReputation reputation = optional.resolve().get();
			reputation.changeReputation(player, faction, amount);
		}
	}

	public static void readPacketData(Collection<Faction> factions) {
		for (Faction faction : factions) {
			CLIENT_FACTIONS.put(faction.getName(), faction);
		}
	}
}
