package mods.thecomputerizer.reputation.capability;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.capability.handlers.ReputationHandler;
import mods.thecomputerizer.reputation.ReputationRef;
import mods.thecomputerizer.reputation.common.ai.ReputationAIPackages;
import mods.thecomputerizer.reputation.common.ai.ServerTrackers;
import mods.thecomputerizer.reputation.registry.RecipeRegistry;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

@ParametersAreNonnullByDefault
public class FactionListener extends SimplePreparableReloadListener<Void> {
    
    private static final Gson GSON = Util.make(() -> {
        GsonBuilder builder = new GsonBuilder();
        builder.disableHtmlEscaping();
        builder.setLenient();
        builder.setPrettyPrinting();
        return builder.create();
    });

    @Override protected @Nonnull Void prepare(ResourceManager manager, ProfilerFiller profiler) {
        return null;
    }

    @Override protected void apply(Void value, ResourceManager manager, ProfilerFiller profiler) {
        Reputation.logInfo("Beginning to read reputation datapack");
        try {
            List<ResourceLocation> checked = new ArrayList<>();
            for(ResourceLocation res : manager.listResources("factions",res1 -> res1.endsWith("json"))) {
                if(!checked.contains(res)) {
                    InputStreamReader reader = new InputStreamReader(getResourceStream(manager,res),UTF_8);
                    ReputationHandler.registerFaction(Faction.fromJson(res,GSON.fromJson(reader,JsonElement.class)));
                    reader.close();
                    checked.add(res);
                }
            }
            List<JsonElement> iconData = new ArrayList<>();
            JsonElement timings = null;
            for(ResourceLocation res : manager.listResources("chat",res1 -> res1.endsWith("json"))) {
                if(!checked.contains(res)) {
                    InputStreamReader reader = new InputStreamReader(getResourceStream(manager,res),UTF_8);
                    JsonElement element = GSON.fromJson(reader,JsonElement.class);
                    if(res.getPath().endsWith("timings.json")) timings = element;
                    else iconData.add(element);
                    reader.close();
                    checked.add(res);
                }
            }
            ServerTrackers.initTimers(timings);
            ServerTrackers.parseChatIcons(iconData);
            Reputation.logInfo("Successfully attached {} files to the chat icon data map",iconData.size());
            try {
                ResourceLocation aiRes = ReputationRef.res("ai.json");
                if(manager.hasResource(aiRes)) {
                    InputStreamReader reader = new InputStreamReader(getResourceStream(manager,aiRes),UTF_8);
                    ReputationAIPackages.buildMobLists(GSON.fromJson(reader,JsonElement.class));
                } else ReputationAIPackages.buildMobLists(JsonParser.parseString("{ }"));
            } catch(Exception ex) {
                throw new RuntimeException("Failed to read AI data!",ex);
            }
            RecipeRegistry.updateCurrencySet(ReputationHandler.getFactionMap().values().stream()
                            .map(Faction::getCurrencyItem).filter(Objects::nonNull).collect(Collectors.toSet()));
        } catch(Exception ex) {
            throw new RuntimeException("Failed to read faction data!",ex);
        }
    }

    private InputStream getResourceStream(ResourceManager manager, ResourceLocation res) throws IOException {
        return manager.getResource(res).getInputStream();
    }
}