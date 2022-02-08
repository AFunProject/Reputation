package mods.thecomputerizer.reputation.api;

import java.util.ArrayList;
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
	private List<ResourceLocation> enemy_names = new ArrayList<>();
	private List<Faction> enemies = new ArrayList<>();
	private List<EntityType<?>> members = new ArrayList<>();

	public Faction(ResourceLocation name, int defaultRep, List<ResourceLocation> enemies, List<EntityType<?>> members) {
		this.name = name;
		this.defaultRep = defaultRep;
		enemy_names.addAll(enemies);
		this.members.addAll(members);
	}

	public ResourceLocation getName() {
		return name == null ? new ResourceLocation("") : name;
	}

	public List<Faction> getEnemies() {
		if (enemies.isEmpty() &! enemy_names.isEmpty()) {
			for (ResourceLocation loc : enemy_names) {
				Faction faction = ReputationHandler.getFaction(loc);
				if (faction != null) enemies.add(faction);
			}
		}
		return enemies;
	}

	public boolean isEnemy(Faction faction) {
		return getEnemies().contains(faction);
	}

	public List<EntityType<?>> getMembers() {
		return members;
	}

	public boolean isMember(LivingEntity entity) {
		return members.contains(entity.getType());
	}

	public int getDefaultRep() {
		return defaultRep;
	}

	public static Faction fromJson(String identifier, String jsonString) {
		try {
			JsonObject json = (JsonObject) JsonParser.parseString(jsonString);
			ResourceLocation name = new ResourceLocation(json.get("name").getAsString());
			int defaultRep = json.get("default_reputation").getAsInt();
			List<EntityType<?>> members = parseMembers(jsonString, json.get("members").getAsJsonArray());
			List<ResourceLocation> enemies = parseResourceArray(jsonString, json.get("enemies").getAsJsonArray());
			return new Faction(name, defaultRep, enemies, members);
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
		builder.append("\"name\": \"").append(name.toString()).append("\", ");
		builder.append("\"default_reputation\": ").append(defaultRep).append(", ");
		builder.append("\"members\": [");
		for (int i = 0; i < members.size(); i++) {
			builder.append("\"").append(Objects.requireNonNull(members.get(i).getRegistryName())).append("\"");
			if (i < members.size()-1) builder.append(", ");
		}
		builder.append("], ");
		builder.append("\"enemies\": [");
		for (int i = 0; i < enemy_names.size(); i++) {
			builder.append("\"").append(enemy_names.get(i).toString()).append("\"");
			if (i < enemy_names.size()-1) builder.append(", ");
		}
		builder.append("]");
		builder.append("}");
		return builder.toString();
	}

}
