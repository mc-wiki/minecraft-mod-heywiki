package wiki.minecraft.heywiki.wiki;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;

public class WikiFamily {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static Codec<WikiFamily> CODEC = RecordCodecBuilder.create(builder ->
            builder
                    .group(
                            Codec.STRING.fieldOf("id").forGetter(family -> family.id),
                            Codec.STRING.listOf().fieldOf("namespace").forGetter(family -> family.namespace),
                            WikiIndividual.CODEC.listOf().fieldOf("wikis").forGetter(family -> family.wikis)
                          )
                    .apply(builder, WikiFamily::new));

    public String id;
    public List<String> namespace;
    public List<WikiIndividual> wikis;

    public WikiFamily(String id, List<String> namespace, List<WikiIndividual> wikis) {
        this.id = id;
        this.namespace = namespace;
        this.wikis = wikis;
    }

    public @Nullable WikiIndividual getLanguageWikiByWikiLanguage(String wikiLanguage) {
        for (WikiIndividual wiki : this.wikis) {
            if (wiki.language.wikiLanguage.equals(wikiLanguage)) {
                return wiki;
            }
        }

        return null;
    }

    public @Nullable WikiIndividual getLanguageWikiByGameLanguage(String gameLanguage) {
        for (WikiIndividual wiki : this.wikis) {
            if (wiki.language.matchLanguage(gameLanguage)) {
                return wiki;
            }
        }

        return null;
    }

    public @Nullable WikiIndividual getMainLanguageWiki() {
        for (WikiIndividual wiki : this.wikis) {
            if (wiki.language.main) {
                return wiki;
            }
        }

        LOGGER.error("Failed to find main language wiki for family {}", this.id);
        return null;
    }
}
