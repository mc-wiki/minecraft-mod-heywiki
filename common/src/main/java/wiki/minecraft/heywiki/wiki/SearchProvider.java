package wiki.minecraft.heywiki.wiki;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.util.HttpUtil;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public interface SearchProvider {
    SequencedSet<Suggestion> search(String term, WikiIndividual wiki) throws IOException, InterruptedException;

    enum ProviderType implements StringIdentifiable {
        MEDIAWIKI("mediawiki"), ALGOLIA("algolia");

        public static final Codec<ProviderType> CODEC = StringIdentifiable.createCodec(ProviderType::values);

        private final String name;

        ProviderType(String name) {
            this.name = name;
        }

        @Override public String asString() {
            return this.name;
        }
    }

    record Suggestion(String title, int index, Optional<String> redirectsTo, Optional<String> imageUrl,
                      Optional<String> realUrl)
            implements Comparable<Suggestion> {
        @Override public int compareTo(@NotNull Suggestion o) {
            return Integer.compare(index, o.index);
        }
    }

    record MediaWikiProvider() implements SearchProvider {
        private static final HeyWikiClient MOD = HeyWikiClient.getInstance();
        public static final String PREFIX_SEARCH_SUGGESTION_URL = "action=query&format=json&formatversion=2" +
                                                                  "&converttitles=true&redirects=true" +
                                                                  "&prop=info|pageimages&inprop=url&pilicense=any&piprop=thumbnail" +
                                                                  "&generator=prefixsearch&gpssearch=%s";
        public static final String SEARCH_SUGGESTION_URL = "action=query&format=json&formatversion=2" +
                                                           "&converttitles=true&redirects=true" +
                                                           "&prop=info|pageimages&inprop=url&pilicense=any&piprop=thumbnail" +
                                                           "&generator=search&gsrsearch=%s";

        @Override public SequencedSet<Suggestion> search(String term, WikiIndividual wiki)
                throws IOException, InterruptedException {
            String apiUrl = wiki.mwApiUrl().orElseThrow();
            URI uri = HttpUtil.uriWithQuery(URI.create(apiUrl), (MOD.config().prefixSearch()
                    ? PREFIX_SEARCH_SUGGESTION_URL
                    : SEARCH_SUGGESTION_URL).formatted(term));

            String response = HttpUtil.request(uri);

            SequencedSet<SearchProvider.Suggestion> suggestions = new TreeSet<>();
            JsonObject root = JsonParser.parseString(response).getAsJsonObject();
            @Nullable JsonObject query = root.getAsJsonObject("query");

            if (query == null) {
                return suggestions;
            }

            @Nullable JsonArray redirects = query.getAsJsonArray("redirects");
            Map<String, Suggestion> redirectMap = redirects != null
                    ? StreamSupport
                    .stream(redirects.spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .map(redirect -> {
                        int index = redirect.get("index").getAsInt();
                        String from = redirect.get("from").getAsString();
                        String to = redirect.get("to").getAsString();
                        return new SearchProvider.Suggestion(from, index, Optional.of(to), Optional.empty(),
                                                             Optional.empty());
                    })
                    .collect(Collectors.toMap((redirect) -> redirect.redirectsTo().orElseThrow(),
                                              suggestion -> suggestion,
                                              (a, b) -> a))
                    : Collections.emptyMap();

            @Nullable JsonArray pages = query.getAsJsonArray("pages");
            if (pages != null) {
                StreamSupport
                        .stream(pages.spliterator(), false)
                        .map(JsonElement::getAsJsonObject)
                        .map(page -> {
                            String title = page.get("title").getAsString();
                            int index = page.get("index").getAsInt();
                            String imageUrl = page.has("thumbnail") ?
                                    page.getAsJsonObject("thumbnail").get("source").getAsString()
                                    : null;
                            String realUrl = page.get("fullurl").getAsString();

                            if (redirectMap.containsKey(title)) {
                                var redirect = redirectMap.get(title);
                                return new SearchProvider.Suggestion(redirect.title(), index, Optional.of(title),
                                                                     Optional.ofNullable(imageUrl),
                                                                     Optional.of(realUrl));
                            }

                            return new SearchProvider.Suggestion(title, index, Optional.empty(),
                                                                 Optional.ofNullable(imageUrl), Optional.of(realUrl));
                        }).forEach(suggestions::add);
            }

            return suggestions;
        }
    }

    record AlgoliaProvider(String apiUrl, String indexName, String apiKey, String appId) implements SearchProvider {
        public static final Codec<AlgoliaProvider> CODEC = RecordCodecBuilder.create(
                builder -> builder.group(Codec.STRING.fieldOf("api_url").forGetter(provider -> provider.apiUrl),
                                         Codec.STRING.fieldOf("index_name").forGetter(provider -> provider.indexName),
                                         Codec.STRING.fieldOf("api_key").forGetter(provider -> provider.apiKey),
                                         Codec.STRING.fieldOf("app_id").forGetter(provider -> provider.appId))
                                  .apply(builder, AlgoliaProvider::new));

        public static final String SUGGESTION_URL = "/1/indexes/%s/query";

        @Override public SequencedSet<Suggestion> search(String term, WikiIndividual wiki)
                throws IOException, InterruptedException {
            URI uri = HttpUtil.uriWithPath(URI.create(apiUrl), String.format(SUGGESTION_URL, indexName));

            try (HttpClient client = HttpClient.newBuilder()
                                               .proxy(ProxySelector.getDefault())
                                               .followRedirects(HttpClient.Redirect.ALWAYS)
                                               .build()) {
                HttpRequest request = HttpRequest.newBuilder(uri)
                                                 .POST(HttpRequest.BodyPublishers.ofString(
                                                         """
                                                         {
                                                           "query": "%s",
                                                           "analytics": false,
                                                           "attributesToHighlight": [],
                                                           "hitsPerPage": 10
                                                         }
                                                         """.formatted(term)))
                                                 .header("User-Agent",
                                                         "HeyWikiMod (+https://github.com/mc-wiki/minecraft-mod-heywiki)")
                                                 .header("X-Algolia-Api-Key", apiKey)
                                                 .header("X-Algolia-Application-Id", appId)
                                                 .build();

                HttpResponse<String> response = client.send(request,
                                                            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (response.statusCode() != 200) {
                    throw new IOException("HTTP " + response.statusCode() + " " + response.body());
                }

                SequencedSet<SearchProvider.Suggestion> suggestions = new TreeSet<>();
                var root = JsonParser.parseString(response.body()).getAsJsonObject();
                @Nullable var hits = root.getAsJsonArray("hits");

                if (hits == null || hits.isEmpty()) return suggestions;

                int counter = 0;
                for (JsonElement hit : hits) {
                    var hitObject = hit.getAsJsonObject();
                    var index = counter;
                    var url = hitObject.get("url").getAsString();
                    var hierarchy = hitObject.get("hierarchy").getAsJsonObject();
                    var title = hierarchy.get("lvl1").getAsString();

                    suggestions.add(new SearchProvider.Suggestion(title, index, Optional.empty(),
                                                                  Optional.empty(), Optional.of(url)));
                    counter++;
                }

                return suggestions;
            }
        }
    }
}
