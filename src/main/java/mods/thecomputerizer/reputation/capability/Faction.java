package mods.thecomputerizer.reputation.capability;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mods.thecomputerizer.reputation.Constants;
import mods.thecomputerizer.reputation.capability.handlers.ReputationHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class Faction {

	private final ResourceLocation id;
	private final String name;
	private final int defaultRep;
	private final int lowerRep;
	private final int higherRep;
	private final int playerJoinThreshold;
	private final Item currencyItem;
	private final List<ResourceLocation> enemy_names = new ArrayList<>();
	private final List<Faction> enemies = new ArrayList<>();
	private final List<EntityType<?>> members = new ArrayList<>();
	private final Map<String, Integer> weightedActions;

	public Faction(ResourceLocation id, String name, int defaultRep, int lowerRep, int higherRep,
				   int playerJoinThreshold, Item currencyItem, Map<String, Integer> weightedActions,
				   List<ResourceLocation> enemies, List<EntityType<?>> members) {
		this.id = id;
		this.name = name;
		this.defaultRep = defaultRep;
		this.lowerRep = lowerRep;
		this.higherRep = higherRep;
		this.playerJoinThreshold = playerJoinThreshold;
		this.currencyItem = Objects.nonNull(currencyItem) ? currencyItem : Items.DIAMOND;
		this.weightedActions = weightedActions;
		this.enemy_names.addAll(enemies);
		this.members.addAll(members);
	}

	public ResourceLocation getID() {
		return Objects.nonNull(this.id) ? this.id : Constants.res("");
	}

	public String getName() {
		return Objects.nonNull(this.name) ? this.name : "";
	}

	public List<Faction> getEnemies() {
		if(this.enemies.isEmpty() && !this.enemy_names.isEmpty()) {
			for(ResourceLocation res : this.enemy_names) {
				Faction faction = ReputationHandler.getFaction(res);
				if(Objects.nonNull(faction)) this.enemies.add(faction);
			}
		}
		return this.enemies;
	}

	@SuppressWarnings("unused")
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

	public boolean canPlayerJoin(Player player) {
		return ReputationHandler.getReputation(player,this)>=this.playerJoinThreshold;
	}

	public Item getCurrencyItem() {
		return this.currencyItem;
	}

	public int getActionWeighting(String action) {
		return this.weightedActions.get(action);
	}

	public static Faction fromJsonAsString(String identifier, String jsonString) {
		try {
			JsonObject json = (JsonObject) JsonParser.parseString(jsonString);
			String name = json.get("name").getAsString();
			ResourceLocation id;
			if(!name.contains(":")) id = Constants.res(json.get("name").getAsString());
			else id = new ResourceLocation(name);
			return fromJson(id, json);
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse faction with id "+identifier+"!",e);
		}
	}

	public static Faction fromJson(ResourceLocation id, JsonElement data) {
		try {
			JsonObject json = data.getAsJsonObject();
			String name = json.get("name").getAsString();
			ResourceLocation faction_id;
			if(!name.contains(":")) faction_id = Constants.res(json.get("name").getAsString());
			else {
				faction_id = new ResourceLocation(name);
				name = name.split(":")[1];
			}
			int defaultRep = json.get("default_reputation").getAsInt();
			int lowerRep = json.get("lower_reputation_bound").getAsInt();
			int higherRep = json.get("upper_reputation_bound").getAsInt();
			int playerJoin = json.has("player_join_threshold") ?
					json.get("player_join_threshold").getAsInt() : Integer.MIN_VALUE;
			Item currency = ForgeRegistries.ITEMS.getValue(new ResourceLocation(json.get("currency").getAsString()));
			HashMap<String, Integer> weighting = new HashMap<>();
			weighting.put("murder", json.get("weighted_murder").getAsInt());
			weighting.put("looting", json.get("weighted_looting").getAsInt());
			weighting.put("fleeing", json.get("weighted_fleeing").getAsInt());
			List<EntityType<?>> members = parseMembers(json);
			List<ResourceLocation> enemies = parseResourceArray("enemies", json);
			return new Faction(faction_id,name, defaultRep,lowerRep,higherRep,playerJoin,currency,weighting,enemies,members);
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse faction with id "+id+"!",e);
		}
	}

	private static List<EntityType<?>> parseMembers(JsonObject json) {
		List<EntityType<?>> members = new ArrayList<>();
		if(json.has("members")) {
			for (ResourceLocation loc : parseResourceArray("members", json)) {
				EntityType<?> entity = ForgeRegistries.ENTITIES.getValue(loc);
				members.add(entity);
			}
		}
		return members;
	}

	private static List<ResourceLocation> parseResourceArray(String element, JsonObject json) {
		List<ResourceLocation> resources = new ArrayList<>();
		if(json.has(element)) {
			for (JsonElement index : json.get(element).getAsJsonArray()) {
				ResourceLocation loc = new ResourceLocation(index.getAsString());
				resources.add(loc);
			}
		}
		return resources;
	}

	public String toJsonString() {
		StringBuilder builder = new StringBuilder("{");
		builder.append("\"name\": \"").append(this.id.toString()).append("\", ").append("\n");
		builder.append("\"default_reputation\": ").append(this.defaultRep).append(", ").append("\n");
		builder.append("\"lower_reputation_bound\": ").append(this.lowerRep).append(", ").append("\n");
		builder.append("\"upper_reputation_bound\": ").append(this.higherRep).append(", ").append("\n");
		builder.append("\"currency\": \"").append(Objects.requireNonNull(this.currencyItem.getRegistryName())).append("\", ").append("\n");
		builder.append("\"weighted_murder\": ").append(this.weightedActions.get("murder")).append(", ").append("\n");
		builder.append("\"weighted_looting\": ").append(this.weightedActions.get("looting")).append(", ").append("\n");
		builder.append("\"weighted_fleeing\": ").append(this.weightedActions.get("fleeing")).append(", ").append("\n");
		builder.append("\"members\": [").append("\n");
		for (int i = 0; i < this.members.size(); i++) {
			builder.append("\"").append(Objects.requireNonNull(this.members.get(i).getRegistryName())).append("\"");
			if (i < this.members.size()-1) builder.append(", ").append("\n");
		}
		builder.append("], \n");
		builder.append("\"enemies\": [").append("\n");
		for (int i = 0; i < this.enemy_names.size(); i++) {
			builder.append("\"").append(this.enemy_names.get(i).toString()).append("\"");
			if (i < this.enemy_names.size()-1) builder.append(", ").append("\n");
		}
		builder.append("]").append("\n");
		builder.append("}");
		return builder.toString();
	}

}
