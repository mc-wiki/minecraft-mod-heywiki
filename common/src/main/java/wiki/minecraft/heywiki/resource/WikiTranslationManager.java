package wiki.minecraft.heywiki.resource;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.mixin.TranslationStorageFactory;
import wiki.minecraft.heywiki.wiki.WikiIndividual;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Manages any additional translation files
 * that need to be loaded for {@link wiki.minecraft.heywiki.wiki.WikiPage WikiPages} resolution.
 */
public class WikiTranslationManager implements SynchronousResourceReloader {
    private static final HeyWikiClient MOD = HeyWikiClient.getInstance();
    private static final Logger LOGGER = LogUtils.getLogger();

    private Map<String, TranslationStorage> translations;

    public WikiTranslationManager() {
    }

    /**
     * Gets the language override storage for the specified wiki.
     *
     * @param wiki The wiki.
     * @return The translation storage. If the language has no override, returns null.
     */
    public @Nullable TranslationStorage getTranslationOverride(WikiIndividual wiki) {
        return wiki.language().langOverride()
                   .map(s -> getTranslations().getOrDefault(s, null))
                   .orElse(null);
    }

    /**
     * A map of translations for each language.
     */
    public Map<String, TranslationStorage> getTranslations() {
        return translations;
    }

    @Override
    public void reload(ResourceManager manager) {
        Map<String, TranslationStorage> translationsNew = new HashMap<>();
        for (String language : decideLanguage()) {
            translationsNew.put(language, loadTranslation(language, manager, true));
        }
        for (String language : MOD.familyManager().getLangOverride()) {
            translationsNew.put(language, loadTranslation(language, manager, false));
        }

        translations = translationsNew;
    }

    private Set<String> decideLanguage() {
        var configLanguage = MOD.config().language();
        if (configLanguage.equals("auto")) {
            return MOD.familyManager().getAllDefaultLanguages();
        } else {
            var mainLanguages = MOD.familyManager().getAllDefaultLanguages();
            var defaultLanguages = MOD.familyManager().getAllDefaultLanguagesFromWikiLanguage(configLanguage);
            mainLanguages.addAll(defaultLanguages);
            return mainLanguages;
        }
    }

    /**
     * Load translations for the specified language from resource packs.
     *
     * @param language        The language code.
     * @param resourceManager The resource manager.
     * @param fallbackEnUs    Whether to fall back to en_us if the specified language is not found.
     * @return The translation storage for the specified language.
     */
    public static TranslationStorage loadTranslation(String language, ResourceManager resourceManager,
                                                     boolean fallbackEnUs) {
        return language.equals("en_us") || !fallbackEnUs
                ? loadTranslationFrom(resourceManager, List.of(language))
                : loadTranslationFrom(resourceManager, List.of("en_us", language));
    }

    /**
     * @see TranslationStorage#load(ResourceManager, List, boolean)
     */
    private static TranslationStorage loadTranslationFrom(ResourceManager resourceManager, List<String> definitions) {
        Map<String, String> map = Maps.newHashMap();

        for (String definition : definitions) {
            String path = String.format(Locale.ROOT, "lang/%s.json", definition);

            for (String namespace : resourceManager.getAllNamespaces()) {
                try {
                    Identifier identifier = Identifier.of(namespace, path);
                    appendTranslationFrom(definition, resourceManager.getAllResources(identifier), map);
                } catch (Exception e) {
                    LOGGER.warn("Skipped language file: {}:{} ({})", namespace, path, e.toString());
                }
            }
        }

        return TranslationStorageFactory.create(ImmutableMap.copyOf(map), false);
    }

    /**
     * @see TranslationStorage#load(String, List, Map)
     */
    private static void appendTranslationFrom(String langCode, List<Resource> resourceRefs,
                                              Map<String, String> translations) {
        for (Resource resource : resourceRefs) {
            try (InputStream inputStream = resource.getInputStream()) {
                Language.load(inputStream, translations::put);
            } catch (IOException e) {
                LOGGER.warn("Failed to load translations for {} from pack {}", langCode, resource.getPackId(), e);
            }
        }
    }
}
