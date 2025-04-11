package mods.thecomputerizer.reputation.client;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class ClientConfigHandler {

    public static final ForgeConfigSpec CONFIG;
    public static final ConfigValue<Boolean> DEBUG;

    static {
        final Builder builder = new Builder();
        builder.push("Reputation ClientConfig");
        DEBUG = builder.comment("Show the debug info").define("debug",false);
        builder.pop();
        CONFIG = builder.build();
    }
}