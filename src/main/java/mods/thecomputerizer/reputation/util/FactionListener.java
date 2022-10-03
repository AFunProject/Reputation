package mods.thecomputerizer.reputation.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import mods.thecomputerizer.reputation.common.ai.ReputationAIPackages;
import mods.thecomputerizer.reputation.common.ai.ServerTrackers;
import mods.thecomputerizer.reputation.common.registration.Tags;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;

import javax.annotation.Nonnull;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FactionListener extends SimplePreparableReloadListener<Void> {
    private static final Gson GSON = Util.make(() -> {
        GsonBuilder builder = new GsonBuilder();
        builder.disableHtmlEscaping();
        builder.setLenient();
        builder.setPrettyPrinting();
        return builder.create();
    });

    @Nonnull
    @Override
    protected Void prepare(@Nonnull ResourceManager rm, @Nonnull ProfilerFiller profiler) {
        return null;
    }

    @Override
    protected void apply(@Nonnull Void value, @Nonnull ResourceManager rm, @Nonnull ProfilerFiller profiler) {
        Reputation.logInfo("Beginning to read reputation datapack");
        try {
            List<ResourceLocation> checked = new ArrayList<>();
            for (ResourceLocation resource : rm.listResources("factions", (location) -> location.endsWith("json"))) {
                if(!checked.contains(resource)) {
                    InputStreamReader reader = new InputStreamReader(rm.getResource(resource).getInputStream(), StandardCharsets.UTF_8);
                    ReputationHandler.registerFaction(Faction.fromJson(resource, GSON.fromJson(reader, JsonElement.class)));
                    reader.close();
                    checked.add(resource);
                }
            }
            for (ResourceLocation resource : rm.listResources("chat", (location) -> location.endsWith("json"))) {
                if(!checked.contains(resource)) {
                    InputStreamReader reader = new InputStreamReader(rm.getResource(resource).getInputStream(), StandardCharsets.UTF_8);
                    ServerTrackers.chatIconJsonData.add(GSON.fromJson(reader, JsonElement.class));
                    reader.close();
                    checked.add(resource);
                }
            }
            Reputation.logInfo("Successfully attached {} files to the chat icon data map",ServerTrackers.chatIconJsonData.size());
            try {
                ResourceLocation ai = new ResourceLocation(ModDefinitions.MODID, "ai.json");
                if(rm.hasResource(ai)) {
                    Resource AI = rm.getResource(ai);
                    InputStreamReader reader = new InputStreamReader(AI.getInputStream(), StandardCharsets.UTF_8);
                    ReputationAIPackages.buildMobLists(GSON.fromJson(reader, JsonElement.class));
                } else ReputationAIPackages.buildMobLists(JsonParser.parseString("{ }"));
            } catch (Exception e) {
                Reputation.logError("'{}'",e.getMessage(),e);
                throw new RuntimeException("Failed to read AI data!");
            }
            Tags.queueCurrencyTagUpdate(ReputationHandler.FACTION_CURRENCY_MAP.keySet().toArray(new Item[0]));
        } catch (Exception e) {
            Reputation.logError("'{}'",e.getMessage(),e);
            throw new RuntimeException("Failed to read faction data!");
        }
    }

}
