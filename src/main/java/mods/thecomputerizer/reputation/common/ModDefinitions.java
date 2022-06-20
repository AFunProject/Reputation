package mods.thecomputerizer.reputation.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class ModDefinitions {

	public static final String MODID = "reputation";
	public static final String NAME = "Reputation";
	public static final String VERSION = "1.18-0.8.0";

	public static List<EntityType<?>> PASSIVE_FLEEING_ENTITIES = new ArrayList<>();
	public static List<EntityType<?>> HOSTILE_ENTITIES = new ArrayList<>();
	public static List<EntityType<?>> PASSIVE_ENTITIES = new ArrayList<>();
	public static List<EntityType<?>> HOSTILE_FLEEING_ENTITIES = new ArrayList<>();
	public static List<EntityType<?>> TRADING_ENTITIES = new ArrayList<>();

	public static String getName(String name) {
		return MODID + "." + name.replace("_", "");
	}

	public static ResourceLocation getResource(String name) {
		return new ResourceLocation(MODID, name.toLowerCase());
	}
}
