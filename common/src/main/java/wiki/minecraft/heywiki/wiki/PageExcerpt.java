package wiki.minecraft.heywiki.wiki;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.util.Util;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static wiki.minecraft.heywiki.HTTPUtils.requestUri;
import static wiki.minecraft.heywiki.resource.PageExcerptCacheManager.excerptCache;

public record PageExcerpt(String title, String excerpt, String imageUrl, int imageWidth, int imageHeight) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static CompletableFuture<PageExcerpt> fromPage(WikiPage page) {
        var wiki = page.wiki;
        var apiUrl = wiki.mwApiUrl();

        var excerptType = wiki.excerpt();

        return excerptType.map(s -> switch (s) {
            case "text_extracts" -> {
                if (apiUrl.isEmpty()) {
                    LOGGER.error("No MediaWiki API provided for TextExtracts");
                    yield null;
                }
                if (excerptCache.containsKey(apiUrl.get() + " " + page.pageName)) {
                    yield CompletableFuture.completedFuture(excerptCache.get(apiUrl.get() + " " + page.pageName));
                }
                yield fromTextExtracts(apiUrl.get(), page.pageName);
            }
            case "none" -> null;
            default -> {
                LOGGER.error("Unknown excerpt type: {}", s);
                yield null;
            }
        }).orElse(null);
    }

    private static CompletableFuture<PageExcerpt> fromTextExtracts(String apiUrl, String pageName) {
        URI uri = URI.create(apiUrl +
                "?action=query&format=json&prop=info%7Cextracts%7Cpageimages%7Crevisions%7Cinfo&formatversion=2" +
                "&redirects=true&exintro=true&exchars=525&explaintext=true&exsectionformat=plain&piprop=thumbnail" +
                "&pithumbsize=640&pilicense=any&rvprop=timestamp&inprop=url&uselang=content&titles=" +
                URLEncoder.encode(pageName, StandardCharsets.UTF_8));
        var executor = Util.getDownloadWorkerExecutor();

        return CompletableFuture.supplyAsync(() -> {
            try {
                String body = requestUri(uri);
                var page = JsonParser.parseString(body).getAsJsonObject()
                                     .get("query").getAsJsonObject()
                                     .get("pages").getAsJsonArray()
                                     .get(0).getAsJsonObject();

                var thumbnail = page.has("thumbnail") ? page.get("thumbnail").getAsJsonObject() : null;

                var excerpt = new PageExcerpt(page.get("title").getAsString(),
                        page.get("extract").getAsString(),
                        thumbnail != null ? thumbnail.get("source").getAsString() : null,
                        thumbnail != null ? thumbnail.get("width").getAsInt() : 0,
                        thumbnail != null ? thumbnail.get("height").getAsInt() : 0);

                excerptCache.put(apiUrl + " " + pageName, excerpt);
                return excerpt;
            } catch (Exception e) {
                LOGGER.error("Failed to fetch page excerpt", e);
                return null;
            }
        }, executor);
    }
}
