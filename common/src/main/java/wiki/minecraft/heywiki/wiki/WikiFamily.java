package wiki.minecraft.heywiki.wiki;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.resource.WikiFamilyManager;

import java.util.List;

/**
 * Represents a family of wikis.
 * A family is a group of {@link WikiIndividual}s that are in different languages but document the same content.
 *
 * @param namespace The namespaces the family is associated with.
 * @param wikis     The wikis in the family.
 * @see WikiIndividual
 * @see WikiFamilyManager
 */
public record WikiFamily(List<String> namespace, List<WikiIndividual> wikis)
        implements Comparable<WikiFamily> {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final HeyWikiClient MOD = HeyWikiClient.getInstance();

    public static final Codec<WikiFamily> CODEC = RecordCodecBuilder
            .create(builder ->
                            builder.group(
                                           Codec.STRING.listOf()
                                                       .fieldOf("namespace")
                                                       .forGetter(family -> family.namespace),
                                           WikiIndividual.CODEC.listOf()
                                                               .fieldOf("wikis")
                                                               .forGetter(family -> family.wikis))
                                   .apply(builder, WikiFamily::new));

    public Identifier id() {
        return MOD.familyManager().getFamilyId(this);
    }

    /**
     * Gets the wiki for the current language in this family.
     *
     * @return The individual wiki.
     */
    public WikiIndividual getWiki() {
        WikiIndividual wiki;

        if (MOD.config().language().equals("auto")) {
            var language = CLIENT.options.language;
            wiki = this.getLanguageWikiByGameLanguage(language);
        } else {
            var language = MOD.config().language();
            wiki = this.getLanguageWikiByWikiLanguage(language);
        }

        if (wiki == null) wiki = this.getMainLanguageWiki();

        return wiki;
    }

    /**
     * Gets the wiki for the given game language.
     *
     * @param gameLanguage The game language code.
     * @return The individual wiki.
     */
    public @Nullable WikiIndividual getLanguageWikiByGameLanguage(String gameLanguage) {
        for (WikiIndividual wiki : this.wikis) {
            if (wiki.language().match(gameLanguage)) {
                return wiki;
            }
        }

        return null;
    }

    /**
     * Gets the wiki for the given wiki language.
     *
     * @param wikiLanguage The wiki language code.
     * @return The individual wiki.
     */
    public @Nullable WikiIndividual getLanguageWikiByWikiLanguage(String wikiLanguage) {
        for (WikiIndividual wiki : this.wikis) {
            if (wiki.language().wikiLanguage().equals(wikiLanguage)) {
                return wiki;
            }
        }

        return null;
    }

    /**
     * Gets the main language wiki for this family.
     *
     * @return The main language wiki.
     */
    public WikiIndividual getMainLanguageWiki() {
        for (WikiIndividual wiki : this.wikis) {
            if (wiki.language().main()) {
                return wiki;
            }
        }

        throw new RuntimeException(
                "Failed to find main language wiki for family " + this.id());
    }

    public String getTranslationKey() {
        return Util.createTranslationKey("wiki_family", this.id());
    }

    @Override
    public int compareTo(@NotNull WikiFamily o) {
        return this.id().compareTo(o.id());
    }
}
