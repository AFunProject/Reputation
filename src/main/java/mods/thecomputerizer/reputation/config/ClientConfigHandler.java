package mods.thecomputerizer.reputation.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfigHandler {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CONFIG;

    public static final ForgeConfigSpec.ConfigValue<Boolean> debug;

    static {
        BUILDER.push("Reputation ClientConfig");
        debug = BUILDER.comment("Show the debug info").define("debug",false);
        BUILDER.pop();
        CONFIG = BUILDER.build();
    }
}
