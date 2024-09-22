package wiki.minecraft.heywiki.wiki;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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
                             Optional<String> versionArticle, Optional<String> excerpt, Optional<String> searchUrl,
                             WikiLanguage language, TitleFormat title) {
    public static final Codec<WikiIndividual> CODEC = RecordCodecBuilder
            .create(builder ->
                            builder.group(
                                           Codec.STRING.fieldOf("article_url")
                                                       .forGetter(wiki -> wiki.articleUrl),
                                           Codec.STRING.optionalFieldOf("mw_api_url")
                                                       .forGetter(wiki -> wiki.mwApiUrl),
                                           Codec.STRING.optionalFieldOf("random_article")
                                                       .forGetter(wiki -> wiki.randomArticle),
                                           Codec.STRING.optionalFieldOf("version_article")
                                                       .forGetter(wiki -> wiki.versionArticle),
                                           Codec.STRING.optionalFieldOf("excerpt")
                                                       .forGetter(wiki -> wiki.excerpt),
                                           Codec.STRING.optionalFieldOf("search_url")
                                                       .forGetter(wiki -> wiki.searchUrl),
                                           WikiLanguage.CODEC.fieldOf("language")
                                                             .forGetter(wiki -> wiki.language),
                                           TitleFormat.CODEC.fieldOf("title")
                                                            .orElse(TitleFormat.DEFAULT)
                                                            .forGetter(wiki -> wiki.title)
                                         )
                                   .apply(builder, WikiIndividual::new));
}
