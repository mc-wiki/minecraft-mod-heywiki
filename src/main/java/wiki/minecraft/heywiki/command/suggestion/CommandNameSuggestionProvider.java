package wiki.minecraft.heywiki.command.suggestion;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CommandNameSuggestionProvider implements SuggestionProvider<SharedSuggestionProvider> {
    private static final Minecraft CLIENT = Minecraft.getInstance();

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<SharedSuggestionProvider> context,
                                                         SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(
                getCommands(Objects.requireNonNull(CLIENT.getConnection()).getCommands()), builder);
    }

    static Set<String> getCommands(CommandDispatcher<?> dispatcher) {
        return dispatcher.getRoot().getChildren().stream().map(CommandNode::getName).collect(Collectors.toSet());
    }
}