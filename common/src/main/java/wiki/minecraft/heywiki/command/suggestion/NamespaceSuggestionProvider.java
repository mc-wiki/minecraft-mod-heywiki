package wiki.minecraft.heywiki.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import wiki.minecraft.heywiki.HeyWikiClient;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class NamespaceSuggestionProvider implements SuggestionProvider<ClientCommandSourceStack> {
    private static final HeyWikiClient MOD = HeyWikiClient.getInstance();
    private static final Minecraft CLIENT = Minecraft.getInstance();
    private final boolean colon;

    public NamespaceSuggestionProvider() {
        this(true);
    }

    public NamespaceSuggestionProvider(boolean colon) {
        this.colon = colon;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ClientCommandSourceStack> context,
                                                         SuggestionsBuilder builder) {
        Set<String> namespaces = CLIENT.getResourceManager().getNamespaces();
        Set<String> availableNamespaces = MOD.familyManager().getAvailableNamespaces();

        Stream<String> intersect = namespaces.stream().filter(availableNamespaces::contains);

        if (this.colon) return SharedSuggestionProvider.suggest(intersect.map(ns -> ns + ":"), builder);
        return SharedSuggestionProvider.suggest(intersect, builder);
    }
}