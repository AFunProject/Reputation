package mods.thecomputerizer.reputation.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfigHandler {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec CONFIG;

    static {
        BUILDER.push("Reputation Common Config");
        BUILDER.pop();
        CONFIG = BUILDER.build();
    }
}
