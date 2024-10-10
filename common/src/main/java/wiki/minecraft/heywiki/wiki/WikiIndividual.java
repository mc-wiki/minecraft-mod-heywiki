package wiki.minecraft.heywiki.wiki;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.slf4j.Logger;
import wiki.minecraft.heywiki.HeyWikiClient;

import java.util.Optional;

/**
 * Represents an individual wiki. This is a single language wiki as part of a {@link WikiFamily}.
 *
 * @param articleUrl     The URL of the article.
 * @param mwApiUrl       The URL of the MediaWiki API.
 * @param randomArticle  The URL of a random article.
 * @param versionArticle The URL of the version article.
 * @param excerpt        The excerpt of the article.
 * @param language       The language matcher.
 * @param title          The title formatter.
 */
public record WikiIndividual(String articleUrl, Optional<String> mwApiUrl, Optional<String> randomArticle,
                             Optional<String> versionArticle, Optional<String> excerpt,
                             Optional<SearchProvider.ProviderType> searchProviderType,
                             Optional<SearchProvider.AlgoliaProvider> algoliaConfig, Optional<String> searchUrl,
                             WikiLanguage language, TitleFormat title) {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<WikiIndividual> CODEC = RecordCodecBuilder.create(
            builder -> builder.group(Codec.STRING.fieldOf("article_url").forGetter(wiki -> wiki.articleUrl),
                                     Codec.STRING.optionalFieldOf("mw_api_url").forGetter(wiki -> wiki.mwApiUrl),
                                     Codec.STRING.optionalFieldOf("random_article")
                                                 .forGetter(wiki -> wiki.randomArticle),
                                     Codec.STRING.optionalFieldOf("version_article")
                                                 .forGetter(wiki -> wiki.versionArticle),
                                     Codec.STRING.optionalFieldOf("excerpt").forGetter(wiki -> wiki.excerpt),
                                     SearchProvider.ProviderType.CODEC.optionalFieldOf("search_provider")
                                                                      .forGetter(wiki -> wiki.searchProviderType),
                                     SearchProvider.AlgoliaProvider.CODEC.optionalFieldOf("algolia")
                                                                         .forGetter(wiki -> wiki.algoliaConfig),
                                     Codec.STRING.optionalFieldOf("search_url").forGetter(wiki -> wiki.searchUrl),
                                     WikiLanguage.CODEC.fieldOf("language").forGetter(wiki -> wiki.language),
                                     TitleFormat.CODEC.fieldOf("title").orElse(TitleFormat.DEFAULT)
                                                      .forGetter(wiki -> wiki.title))
                              .apply(builder, WikiIndividual::new));

    public Optional<SearchProvider> searchProvider() {
        if (searchProviderType.isEmpty() && searchUrl.isPresent()) {
            if (mwApiUrl().isPresent()) {
                HeyWikiClient.deprecatedWarning(LOGGER, "search_provider should be specified");

                return Optional.of(new SearchProvider.MediaWikiProvider());
            } else return Optional.empty();
        }

        return searchProviderType.flatMap(providerType -> switch (providerType) {
            case MEDIAWIKI -> Optional.of(new SearchProvider.MediaWikiProvider());
            case ALGOLIA -> {
                if (algoliaConfig.isEmpty()) {
                    HeyWikiClient.deprecatedWarning(LOGGER, "algolia should be specified");
                    yield Optional.empty();
                }

                yield algoliaConfig;
            }
        });
    }
}