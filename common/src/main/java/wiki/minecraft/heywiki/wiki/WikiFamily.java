package wiki.minecraft.heywiki.wiki;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import wiki.minecraft.heywiki.HeyWikiConfig;

import java.util.List;

import static wiki.minecraft.heywiki.resource.WikiFamilyConfigManager.activeWikis;

public record WikiFamily(String id, List<String> namespace, List<WikiIndividual> wikis) {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    public static Codec<WikiFamily> CODEC = RecordCodecBuilder.create(builder ->
            builder
                    .group(
                            Codec.STRING.fieldOf("id").forGetter(family -> family.id),
                            Codec.STRING.listOf().fieldOf("namespace").forGetter(family -> family.namespace),
                            WikiIndividual.CODEC.listOf().fieldOf("wikis").forGetter(family -> family.wikis)
                          )
                    .apply(builder, WikiFamily::new));

    public @Nullable WikiIndividual getLanguageWikiByWikiLanguage(String wikiLanguage) {
        for (WikiIndividual wiki : this.wikis) {
            if (wiki.language().wikiLanguage().equals(wikiLanguage)) {
                return wiki;
            }
        }

        return null;
    }

    public @Nullable WikiIndividual getLanguageWikiByGameLanguage(String gameLanguage) {
        for (WikiIndividual wiki : this.wikis) {
            if (wiki.language().matchLanguage(gameLanguage)) {
                return wiki;
            }
        }

        return null;
    }

    public @Nullable WikiIndividual getMainLanguageWiki() {
        for (WikiIndividual wiki : this.wikis) {
            if (wiki.language().main()) {
                return wiki;
            }
        }

        LOGGER.error("Failed to find main language wiki for family {}", this.id);
        return null;
    }


    public WikiIndividual getWiki() {
        WikiIndividual wiki;

        if (HeyWikiConfig.language.equals("auto")) {
            var language = CLIENT.options.language;
            wiki = this.getLanguageWikiByGameLanguage(language);
        } else {
            var language = HeyWikiConfig.language;
            wiki = this.getLanguageWikiByWikiLanguage(language);
        }

        if (wiki == null) wiki = this.getMainLanguageWiki();
        if (wiki == null) {
            LOGGER.error("Failed to find wiki for language {}", HeyWikiConfig.language);
            return null;
        }

        for (String namespace : this.namespace()) {
            activeWikis.put(namespace, wiki);
        }

        return wiki;
    }
}
