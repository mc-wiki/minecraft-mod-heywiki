package wiki.minecraft.heywiki.wiki;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * Represents a wiki language.
 *
 * @param wikiLanguage    The wiki language.
 * @param main            Whether this is the main language.
 * @param defaultLanguage The default language.
 * @param regex           The regex to match the language.
 * @param exclude         The regex to exclude the language.
 * @param langOverride    The language override.
 */
public record LanguageMatcher(String wikiLanguage, Boolean main, String defaultLanguage, String regex,
                              Optional<String> exclude, Optional<String> langOverride) {
    public static Codec<LanguageMatcher> CODEC = RecordCodecBuilder
            .create(builder ->
                            builder
                                    .group(
                                            Codec.STRING.fieldOf("wiki_language")
                                                        .forGetter(matcher -> matcher.wikiLanguage),
                                            Codec.BOOL.fieldOf("main")
                                                      .orElse(false)
                                                      .forGetter(matcher -> matcher.main),
                                            Codec.STRING.fieldOf("default")
                                                        .forGetter(matcher -> matcher.defaultLanguage),
                                            Codec.STRING.fieldOf("regex")
                                                        .forGetter(matcher -> matcher.regex),
                                            Codec.STRING.optionalFieldOf("exclude")
                                                        .forGetter(matcher -> matcher.exclude),
                                            Codec.STRING.optionalFieldOf("lang_override")
                                                        .forGetter(matcher -> matcher.langOverride)
                                          )
                                    .apply(builder, LanguageMatcher::new));

    /**
     * Whether a given language code matches this language.
     *
     * @param language The language code.
     * @return Whether the language matches.
     */
    public Boolean matchLanguage(String language) {
        if (this.exclude.isEmpty()) return language.matches(this.regex);
        return language.matches(this.regex) && !language.matches(String.valueOf(this.exclude));
    }
}
