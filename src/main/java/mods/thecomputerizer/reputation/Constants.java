package mods.thecomputerizer.reputation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class Constants {

	public static final String MODID = "reputation";
	public static final String NAME = "Reputation";
	public static final Logger LOGGER = LogManager.getLogger(NAME);
	private static final Random RANDOM = new Random();

	public static float floatRand() {
		return RANDOM.nextFloat();
	}
	public static float floatRand(float bound) {
		return RANDOM.nextFloat(bound);
	}
	public static float floatRand(float min, float max) {
		return Mth.randomBetween(RANDOM,min,max);
	}
	public static int intRand(int bound) {
		return RANDOM.nextInt(bound);
	}

	public static ResourceLocation res(String name) {
		return new ResourceLocation(MODID, name.toLowerCase());
	}
}
