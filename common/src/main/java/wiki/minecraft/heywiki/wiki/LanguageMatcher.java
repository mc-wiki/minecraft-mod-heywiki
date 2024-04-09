package wiki.minecraft.heywiki.wiki;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public class LanguageMatcher {
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
