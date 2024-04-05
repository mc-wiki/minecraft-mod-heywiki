package wiki.minecraft.heywiki.wiki;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

public class WikiFamily {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static Codec<WikiFamily> CODEC = RecordCodecBuilder.create(builder ->
            builder
                    .group(
                            Codec.STRING.fieldOf("id").forGetter(family -> family.id),
                            Codec.STRING.fieldOf("namespace").forGetter(family -> family.namespace),
                            IndividualWiki.CODEC.listOf().fieldOf("wikis").forGetter(family -> family.wikis)
                          )
                    .apply(builder, WikiFamily::new));

    public String id;
    public String namespace;
    public List<IndividualWiki> wikis;

    public WikiFamily(String id, String namespace, List<IndividualWiki> wikis) {
        this.id = id;
        this.namespace = namespace;
        this.wikis = wikis;
    }

    public @Nullable IndividualWiki getLanguageWikiByWikiLanguage(String wikiLanguage) {
        for (IndividualWiki wiki : this.wikis) {
            if (wiki.language.wikiLanguage.equals(wikiLanguage)) {
                return wiki;
            }
        }

        return null;
    }

    public @Nullable IndividualWiki getLanguageWikiByGameLanguage(String gameLanguage) {
        for (IndividualWiki wiki : this.wikis) {
            if (wiki.language.matchLanguage(gameLanguage)) {
                return wiki;
            }
        }

        return null;
    }

    public @Nullable IndividualWiki getMainLanguageWiki() {
        for (IndividualWiki wiki : this.wikis) {
            if (wiki.language.main) {
                return wiki;
            }
        }

        LOGGER.error("Failed to find main language wiki for family {}", this.id);
        return null;
    }

    public static class IndividualWiki {
        public static Codec<IndividualWiki> CODEC = RecordCodecBuilder.create(builder ->
                builder
                        .group(
                                Codec.STRING.fieldOf("url_pattern").forGetter(wiki -> wiki.urlPattern),
                                LanguageMatcher.CODEC.fieldOf("language").forGetter(wiki -> wiki.language)
                              )
                        .apply(builder, IndividualWiki::new));
        public String urlPattern;
        public LanguageMatcher language;

        public IndividualWiki(String urlPattern, LanguageMatcher language) {
            this.urlPattern = urlPattern;
            this.language = language;
        }
    }

    public static class LanguageMatcher {
        public static Codec<LanguageMatcher> CODEC = RecordCodecBuilder.create(builder ->
                builder
                        .group(
                                Codec.STRING.fieldOf("wiki_language").forGetter(matcher -> matcher.wikiLanguage),
                                Codec.BOOL.fieldOf("main").orElse(false).forGetter(matcher -> matcher.main),
                                Codec.STRING.fieldOf("default").forGetter(matcher -> matcher.defaultLanguage),
                                Codec.STRING.fieldOf("regex").forGetter(matcher -> matcher.regex),
                                Codec.STRING.optionalFieldOf("exclude").forGetter(matcher -> matcher.exclude)
                              )
                        .apply(builder, LanguageMatcher::new));

        public String wikiLanguage;
        public Boolean main;
        public String defaultLanguage;
        public String regex;
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public Optional<String> exclude;

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public LanguageMatcher(String wikiLanguage, Boolean main, String defaultLanguage, String regex, Optional<String> exclude) {
            this.wikiLanguage = wikiLanguage;
            this.main = main;
            this.defaultLanguage = defaultLanguage;
            this.regex = regex;
            this.exclude = exclude;
        }

        public Boolean matchLanguage(String language) {
            if (this.exclude.isEmpty()) return language.matches(this.regex);
            return language.matches(this.regex) && !language.matches(String.valueOf(this.exclude));
        }
    }
}
