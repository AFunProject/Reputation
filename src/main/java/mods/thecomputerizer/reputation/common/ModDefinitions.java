package mods.thecomputerizer.reputation.common;

import net.minecraft.resources.ResourceLocation;

public class ModDefinitions {

	public static final String MODID = "reputation";

	public static final String NAME = "Reputation";

	public static String getName(String name) {
		return MODID + "." + name.replace("_", "");
	}

	public static ResourceLocation getResource(String name) {
		return new ResourceLocation(MODID, name.toLowerCase());
	}
}
