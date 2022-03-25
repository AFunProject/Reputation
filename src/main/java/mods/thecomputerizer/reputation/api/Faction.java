package mods.thecomputerizer.reputation.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import mods.thecomputerizer.reputation.Reputation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

public class Faction {

	private ResourceLocation name;
	private int defaultRep;
	private int lowerRep;
	private int higherRep;
	private List<ResourceLocation> enemy_names = new ArrayList<>();
	private List<Faction> enemies = new ArrayList<>();
	private List<EntityType<?>> members = new ArrayList<>();
	private HashMap<String, Integer> weightedActions;

	public Faction(ResourceLocation name, int defaultRep, int lowerRep, int higherRep, HashMap<String, Integer> weightedActions, List<ResourceLocation> enemies, List<EntityType<?>> members) {
		this.name = name;
		this.defaultRep = defaultRep;
		this.lowerRep = lowerRep;
		this.higherRep = higherRep;
		this.weightedActions = weightedActions;
		this.enemy_names.addAll(enemies);
		this.members.addAll(members);
	}

	public ResourceLocation getName() {
		return this.name == null ? new ResourceLocation("") : this.name;
	}

	public List<Faction> getEnemies() {
		if (this.enemies.isEmpty() &! this.enemy_names.isEmpty()) {
			for (ResourceLocation loc : this.enemy_names) {
				Faction faction = ReputationHandler.getFaction(loc);
				if (faction != null) this.enemies.add(faction);
			}
		}
		return this.enemies;
	}

	public boolean isEnemy(Faction faction) {
		return getEnemies().contains(faction);
	}

	public List<EntityType<?>> getMembers() {
		return this.members;
	}

	public boolean isMember(LivingEntity entity) {
		return this.members.contains(entity.getType());
	}

	public int getDefaultRep() {
		return this.defaultRep;
	}

	public int getLowerRep() {
		return this.lowerRep;
	}

	public int getHigherRep() {
		return this.higherRep;
	}

	public int getActionWeighting(String action) {
		return this.weightedActions.get(action);
	}

	public static Faction fromJson(String identifier, String jsonString) {
		try {
			JsonObject json = (JsonObject) JsonParser.parseString(jsonString);
			ResourceLocation name = new ResourceLocation(json.get("name").getAsString());
			int defaultRep = json.get("default_reputation").getAsInt();
			int lowerRep = json.get("lower_reputation_bound").getAsInt();
			int higherRep = json.get("upper_reputation_bound").getAsInt();
			HashMap<String, Integer> weighting = new HashMap<>();
			weighting.put("murder", json.get("weighted_murder").getAsInt());
			weighting.put("looting", json.get("weighted_looting").getAsInt());
			List<EntityType<?>> members = parseMembers(jsonString, json.get("members").getAsJsonArray());
			List<ResourceLocation> enemies = parseResourceArray(jsonString, json.get("enemies").getAsJsonArray());
			return new Faction(name, defaultRep, lowerRep, higherRep, weighting, enemies, members);
		} catch (Exception e) {
			Reputation.logError("Failed to add faction " + identifier, e);
			Reputation.logInfo(jsonString);
		}
		return null;
	}

	private static List<EntityType<?>> parseMembers(String filename, JsonArray array) {
		List<EntityType<?>> members = new ArrayList<>();
		for (ResourceLocation loc : parseResourceArray(filename, array)) {
			try {
				EntityType<?> entity = ForgeRegistries.ENTITIES.getValue(loc);
				members.add(entity);
			} catch (Exception e) {
				Reputation.logError("Failed to add entry "+ loc + " to faction, entity does not exist " + filename, e);
				Reputation.logInfo(array);
			}
		}
		return members;
	}

	private static List<ResourceLocation> parseResourceArray(String filename, JsonArray array) {
		List<ResourceLocation> resources = new ArrayList<>();
		for (JsonElement json : array) {
			try {
				ResourceLocation loc = new ResourceLocation(json.getAsString());
				resources.add(loc);
			} catch (Exception e) {
				Reputation.logError("Failed to add entry "+ json.toString() + " to faction " + filename, e);
				Reputation.logInfo(array);
			}
		}
		return resources;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("{");
		builder.append("\"name\": \"").append(this.name.toString()).append("\", ");
		builder.append("\"default_reputation\": ").append(this.defaultRep).append(", ");
		builder.append("\"lower_reputation_bound\": ").append(this.lowerRep).append(", ");
		builder.append("\"upper_reputation_bound\": ").append(this.higherRep).append(", ");
		builder.append("\"weighted_murder\": ").append(this.weightedActions.get("murder")).append(", ");
		builder.append("\"weighted_looting\": ").append(this.weightedActions.get("looting")).append(", ");
		builder.append("\"members\": [");
		for (int i = 0; i < this.members.size(); i++) {
			builder.append("\"").append(Objects.requireNonNull(this.members.get(i).getRegistryName())).append("\"");
			if (i < this.members.size()-1) builder.append(", ");
		}
		builder.append("], ");
		builder.append("\"enemies\": [");
		for (int i = 0; i < this.enemy_names.size(); i++) {
			builder.append("\"").append(this.enemy_names.get(i).toString()).append("\"");
			if (i < this.enemy_names.size()-1) builder.append(", ");
		}
		builder.append("]");
		builder.append("}");
		return builder.toString();
	}

}
