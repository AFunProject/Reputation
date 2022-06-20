package mods.thecomputerizer.reputation.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import mods.thecomputerizer.reputation.common.ai.ReputationAIPackages;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

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
        for(String string : rm.getNamespaces()) Reputation.logInfo(string);
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
                    Reputation.chatIconData.add(GSON.fromJson(reader, JsonElement.class));
                    reader.close();
                    checked.add(resource);
                }
            }
            try {
                Resource AI = rm.getResource(new ResourceLocation(ModDefinitions.MODID, "ai.json"));
                InputStreamReader reader = new InputStreamReader(AI.getInputStream(), StandardCharsets.UTF_8);
                ReputationAIPackages.buildMobLists(GSON.fromJson(reader, JsonElement.class));
            } catch (Exception e) {
                Reputation.logError("Forcing error log to print for error: "+e.getMessage(),e);
                for(StackTraceElement element : e.getStackTrace()) {
                    Reputation.logError(element,e);
                }
                throw new RuntimeException("Failed to read AI data!");
            }
        } catch (Exception e) {
            Reputation.logError("Forcing error log to print for error: "+e.getMessage(),e);
            for(StackTraceElement element : e.getStackTrace()) {
                Reputation.logError(element,e);
            }
            throw new RuntimeException("Failed to read faction data!");
        }
    }

}
