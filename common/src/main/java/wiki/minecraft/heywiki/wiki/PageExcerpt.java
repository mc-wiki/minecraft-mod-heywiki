package wiki.minecraft.heywiki.wiki;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.util.HttpUtil;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Represents an excerpt of a wiki page.
 *
 * @param title       The title of the page.
 * @param excerpt     The excerpt of the page.
 * @param imageUrl    The URL of the image.
 * @param imageWidth  The width of the image.
 * @param imageHeight The height of the image.
 */
public record PageExcerpt(String title, String excerpt, String imageUrl, int imageWidth, int imageHeight) {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final HeyWikiClient MOD = HeyWikiClient.getInstance();
    private static final Cache<String, Optional<CompletableFuture<PageExcerpt>>> excerptCache = CacheBuilder.newBuilder()
                                                                                                            .maximumSize(
                                                                                                                    100)
                                                                                                            .expireAfterAccess(
                                                                                                                    10,
                                                                                                                    TimeUnit.MINUTES)
                                                                                                            .build();

    /**
     * Creates a page excerpt from a wiki page.
     *
     * @param page The wiki page.
     * @return The page excerpt.
     */
    public static Optional<CompletableFuture<PageExcerpt>> fromPage(WikiPage page) {
        var wiki = page.wiki();
        var apiUrl = wiki.mwApiUrl();
        var excerptType = wiki.excerpt();

        try {
            return excerptCache.get(wiki.articleUrl() + " " + page.pageName(), () -> excerptType.map(s -> switch (s) {
                case "text_extracts" -> {
                    if (apiUrl.isEmpty()) {
                        LOGGER.error("No MediaWiki API provided for TextExtracts");
                        yield null;
                    }
                    yield fromTextExtracts(apiUrl.get(), page.pageName(), wiki.language().wikiLanguage());
                }
                case "none" -> null;
                default -> {
                    LOGGER.error("Unknown excerpt type: {}", s);
                    yield null;
                }
            }));
        } catch (ExecutionException e) {
            LOGGER.error("Failed to get page excerpt", e);
            return Optional.empty();
        }
    }

    private static CompletableFuture<PageExcerpt> fromTextExtracts(String apiUrl, String pageName, String language) {
        URI uri = URI.create(apiUrl +
                             "?action=query&format=json&prop=info%7Cextracts%7Cpageimages%7Crevisions%7Cinfo&formatversion=2" +
                             "&redirects=true&exintro=true&exchars=525&explaintext=true&exsectionformat=plain&piprop=thumbnail" +
                             "&pithumbsize=640&pilicense=any&rvprop=timestamp&inprop=url&uselang=content&titles=" +
                             URLEncoder.encode(pageName, StandardCharsets.UTF_8) +
                             (language.equals("zh") ? "&converttitles=true&variant=" +
                                                      resolveZhVariant(MOD.config().zhVariant()) : ""));
        var executor = Util.getDownloadWorkerExecutor();

        return CompletableFuture.supplyAsync(() -> {
            try {
                String body = HttpUtil.request(uri);
                var page = JsonParser.parseString(body).getAsJsonObject()
                                     .get("query").getAsJsonObject()
                                     .get("pages").getAsJsonArray()
                                     .get(0).getAsJsonObject();

                var thumbnail = page.has("thumbnail") ? page.get("thumbnail").getAsJsonObject() : null;

                return new PageExcerpt(page.get("title").getAsString(),
                                       page.get("extract").getAsString(),
                                       thumbnail != null ? thumbnail.get("source").getAsString() : null,
                                       thumbnail != null ? thumbnail.get("width").getAsInt() : 0,
                                       thumbnail != null ? thumbnail.get("height").getAsInt() : 0);
            } catch (Exception e) {
                LOGGER.error("Failed to fetch page excerpt", e);
                return null;
            }
        }, executor);
    }

    private static String resolveZhVariant(String variant) {
        if (variant.equals("auto")) {
            return switch (CLIENT.options.language) {
                case "zh_cn" -> "zh-cn";
                case "zh_tw" -> "zh-tw";
                case "zh_hk" -> "zh-hk";
                default -> "zh";
            };
        }
        return variant;
    }
}
