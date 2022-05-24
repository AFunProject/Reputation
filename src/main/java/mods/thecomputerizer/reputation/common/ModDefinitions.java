package mods.thecomputerizer.reputation.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import javax.swing.text.html.parser.Entity;
import java.util.ArrayList;
import java.util.List;

public class ModDefinitions {

	public static final String MODID = "reputation";
	public static final String NAME = "Reputation";

	public static List<EntityType<?>> PASSIVE_FLEEING_ENTITIES = new ArrayList<>();
	public static List<EntityType<?>> HOSTILE_ENTITIES = new ArrayList<>();
	public static List<EntityType<?>> PASSIVE_NEUTRAL_ENTITIES = new ArrayList<>();
	public static List<EntityType<?>> PASSIVE_GOOD_ENTITIES = new ArrayList<>();
	public static List<EntityType<?>> HOSTILE_FLEEING_ENTITIES = new ArrayList<>();

	public static String getName(String name) {
		return MODID + "." + name.replace("_", "");
	}

	public static ResourceLocation getResource(String name) {
		return new ResourceLocation(MODID, name.toLowerCase());
	}
}
