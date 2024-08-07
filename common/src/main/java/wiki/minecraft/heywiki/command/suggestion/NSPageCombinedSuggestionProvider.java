package wiki.minecraft.heywiki.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import wiki.minecraft.heywiki.resource.WikiFamilyConfigManager;
import wiki.minecraft.heywiki.wiki.WikiIndividual;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static wiki.minecraft.heywiki.resource.WikiFamilyConfigManager.activeWikis;

public class NSPageCombinedSuggestionProvider implements SuggestionProvider<ClientCommandSourceStack> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ClientCommandSourceStack> context,
                                                         SuggestionsBuilder builder) {
        String remaining = builder.getRemaining();
        WikiIndividual wiki = activeWikis.get("minecraft");
        if (wiki == null) return new NamespaceSuggestionProvider().getSuggestions(context, builder);
        String apiUrl = wiki.mwApiUrl().orElse(null);
        if (apiUrl == null) return new NamespaceSuggestionProvider().getSuggestions(context, builder);

        if (!remaining.contains(":")) {
            return new PageNameSuggestionProvider(() -> URI.create(apiUrl))
                    .getSuggestions(context, builder)
                    .thenApplyAsync(suggestions -> {
                        List<Suggestion> list = new NamespaceSuggestionProvider().getSuggestions(context, builder)
                                                                                 .join().getList();
                        list.addAll(suggestions.getList());
                        return new Suggestions(StringRange.at(builder.getStart()), list);
                    });
        }

        String[] split = remaining.split(":", 2);
        if (WikiFamilyConfigManager.getAvailableNamespaces().contains(split[0])) {
            SuggestionsBuilder fakeBuilder = new SuggestionsBuilder(builder.getInput(),
                                                                    builder.getStart() + split[0].length() + 1);
            return new PageNameSuggestionProvider(() -> URI.create(apiUrl))
                    .getSuggestions(context, fakeBuilder);
        }

        return new NamespaceSuggestionProvider().getSuggestions(context, builder);
    }
}