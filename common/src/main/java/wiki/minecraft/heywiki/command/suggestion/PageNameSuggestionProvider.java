package wiki.minecraft.heywiki.command.suggestion;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.logging.LogUtils;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import net.minecraft.Util;
import org.slf4j.Logger;
import wiki.minecraft.heywiki.util.CachedDebouncer;
import wiki.minecraft.heywiki.util.HttpUtil;

import java.io.StringReader;
import java.net.URI;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import static wiki.minecraft.heywiki.util.HttpUtil.encodeUrl;

public class PageNameSuggestionProvider implements SuggestionProvider<ClientCommandSourceStack> {
    private static final long TIMEOUT = 400;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final String SUGGESTION_URL = "action=opensearch&format=json&formatversion=2&limit=10&search=%s";
    private static final CachedDebouncer<String, Suggestions> debouncer = new CachedDebouncer<>(TIMEOUT);

    private final Callable<URI> uriProvider;

    /**
     * Creates a new page name suggestion provider.
     *
     * @param uriProvider The URI provider.
     */
    public PageNameSuggestionProvider(Callable<URI> uriProvider) {
        this.uriProvider = uriProvider;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ClientCommandSourceStack> context,
                                                         SuggestionsBuilder builder) {
        return CompletableFuture.supplyAsync(() -> {
            if (builder.getRemaining().isEmpty()) {
                return builder.build();
            }

            try {
                Optional<Suggestions> result = debouncer.get(builder.getInput(), () -> {
                    String remaining = builder.getRemaining();
                    URI uri = HttpUtil.uriWithQuery(
                            this.uriProvider.call(),
                            String.format(SUGGESTION_URL, encodeUrl(remaining)));

                    String response = HttpUtil.request(uri);

                    JsonReader reader = GSON.newJsonReader(new StringReader(response));
                    reader.beginArray();
                    reader.skipValue();
                    reader.beginArray();
                    HashSet<String> suggestions = new HashSet<>();

                    while (reader.hasNext()) {
                        suggestions.add(reader.nextString());
                    }
                    reader.close();
                    suggestions.forEach(builder::suggest);

                    return builder.build();
                });

                return result.orElseGet(builder::build);
            } catch (Exception e) {
                LOGGER.warn("Failed to get suggestions", e);
                return builder.build();
            }
        }, Util.ioPool());
    }
}