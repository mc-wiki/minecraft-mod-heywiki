package wiki.minecraft.heywiki.wiki;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * Represents a wiki language.
 *
 * @param wikiLanguage    The language code for the wiki language.
 * @param main            Whether this is the main language wiki.
 * @param defaultLanguage The default game language that corresponds to this wiki language.
 * @param regex           A regular expression the language code has to match.
 * @param exclude         An optional regular expression the language code must not match.
 * @param langOverride    The name for a translation file that overrides the game's builtin translation file.
 */
public record WikiLanguage(String wikiLanguage, Boolean main, String defaultLanguage, String regex,
                           Optional<String> exclude, Optional<String> langOverride) {
    public static final Codec<WikiLanguage> CODEC = RecordCodecBuilder
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
                                    .apply(builder, WikiLanguage::new));

    /**
     * Whether a given language code matches this language.
     *
     * @param language The language code.
     * @return Whether the language matches.
     */
    public boolean match(String language) {
        if (this.exclude.isEmpty()) return language.matches(this.regex);
        return language.matches(this.regex) && !language.matches(String.valueOf(this.exclude));
    }
}
