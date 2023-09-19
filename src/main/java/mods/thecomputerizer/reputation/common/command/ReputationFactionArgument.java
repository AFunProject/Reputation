package mods.thecomputerizer.reputation.common.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import mods.thecomputerizer.reputation.capability.Faction;
import mods.thecomputerizer.reputation.capability.handlers.ReputationHandler;
import mods.thecomputerizer.reputation.client.ClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ReputationFactionArgument implements ArgumentType<ResourceLocation> {

    private static final List<ResourceLocation> FACTIONS = new ArrayList<>();

    public static ReputationFactionArgument id() {
        for(Faction f : ReputationHandler.getFactionMap().values()) FACTIONS.add(f.getID());
        return new ReputationFactionArgument();
    }

    @Override
    public ResourceLocation parse(StringReader reader) throws CommandSyntaxException {
        return ResourceLocation.read(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        Minecraft mc = Minecraft.getInstance();
        return context.getSource() instanceof SharedSuggestionProvider ? (Objects.nonNull(mc.player) ?
                SharedSuggestionProvider.suggestResource(ClientEvents.CLIENT_FACTIONS.keySet().stream(),builder) :
                SharedSuggestionProvider.suggestResource(FACTIONS.stream(),builder)) : Suggestions.empty();
    }

    @Override
    public Collection<String> getExamples() {
        return new ArrayList<>();
    }
}
