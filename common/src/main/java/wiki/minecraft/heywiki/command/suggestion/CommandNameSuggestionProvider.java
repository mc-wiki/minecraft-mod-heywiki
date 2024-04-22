package wiki.minecraft.heywiki.command.suggestion;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CommandNameSuggestionProvider implements SuggestionProvider<ClientCommandSourceStack> {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    static Set<String> getCommands(CommandDispatcher<?> dispatcher) {
        return dispatcher.getRoot().getChildren().stream().map(CommandNode::getName).collect(Collectors.toSet());
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ClientCommandSourceStack> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(getCommands(Objects.requireNonNull(CLIENT.getNetworkHandler()).getCommandDispatcher()), builder);
    }
}