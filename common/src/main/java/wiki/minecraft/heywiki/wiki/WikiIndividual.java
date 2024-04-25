package wiki.minecraft.heywiki.wiki;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record WikiIndividual(String articleUrl, Optional<String> mwApiUrl, String randomArticle,
                             Optional<String> versionArticle,
                             LanguageMatcher language) {
    public static Codec<WikiIndividual> CODEC = RecordCodecBuilder.create(builder ->
            builder
                    .group(
                            Codec.STRING.fieldOf("article_url").forGetter(wiki -> wiki.articleUrl),
                            Codec.STRING.optionalFieldOf("mw_api_url").forGetter(wiki -> wiki.mwApiUrl),
                            Codec.STRING.fieldOf("random_article").forGetter(wiki -> wiki.randomArticle),
                            Codec.STRING.optionalFieldOf("version_article").forGetter(wiki -> wiki.versionArticle),
                            LanguageMatcher.CODEC.fieldOf("language").forGetter(wiki -> wiki.language)
                          )
                    .apply(builder, WikiIndividual::new));
}
