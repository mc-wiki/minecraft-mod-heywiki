package wiki.minecraft.heywiki.command.suggestion;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.logging.LogUtils;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import net.minecraft.util.Util;
import org.slf4j.Logger;

import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import static wiki.minecraft.heywiki.HTTPUtils.requestUri;
import static wiki.minecraft.heywiki.resource.PageNameSuggestionCacheManager.suggestionsCache;

public class PageNameSuggestionProvider implements SuggestionProvider<ClientCommandSourceStack> {
    public static final long TIMEOUT = 400;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final String SUGGESTION_URL = "action=opensearch&format=json&formatversion=2&limit=10&search=%s";
    private static volatile String lastInput = "";
    private final Callable<URI> uriProvider;

    public PageNameSuggestionProvider(Callable<URI> uriProvider) {
        this.uriProvider = uriProvider;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ClientCommandSourceStack> context,
                                                         SuggestionsBuilder builder) {
        return CompletableFuture.supplyAsync(() -> {
            lastInput = builder.getInput();
            if (suggestionsCache.containsKey(builder.getInput())) {
                return suggestionsCache.get(builder.getInput());
            }
            try {
                Thread.sleep(TIMEOUT);
            } catch (InterruptedException e) {
                LOGGER.warn("Interrupted while waiting for debounce", e);
            }
            if (!builder.getInput().equals(lastInput)) {
                return builder.build();
            }
            String remaining = builder.getRemaining();
            if (remaining.isEmpty()) return builder.build();
            try {
                URI uri = uriWithQuery(this.uriProvider.call(), String.format(SUGGESTION_URL,
                                                                              URLEncoder.encode(remaining,
                                                                                                StandardCharsets.UTF_8)));

                String response = requestUri(uri);

                JsonReader reader = GSON.newJsonReader(new StringReader(response));
                reader.beginArray();
                reader.skipValue();
                reader.beginArray();
                HashSet<String> suggestions = new HashSet<>();

                if (!builder.getInput().equals(lastInput)) {
                    return builder.build();
                }

                while (reader.hasNext()) {
                    suggestions.add(reader.nextString());
                }
                reader.close();
                suggestions.forEach(builder::suggest);
                suggestionsCache.put(builder.getInput(), builder.build());
            } catch (Exception e) {
                LOGGER.warn("Failed to get suggestions", e);
            }
            return builder.build();
        }, Util.getDownloadWorkerExecutor());
    }

    private static URI uriWithQuery(URI uri, String query) {
        try {
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), query, uri.getFragment());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}