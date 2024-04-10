package wiki.minecraft.heywiki.resource;

import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import wiki.minecraft.heywiki.HeyWikiConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static wiki.minecraft.heywiki.resource.WikiFamilyConfigManager.*;

public class WikiTranslationManager implements SynchronousResourceReloader {
    public static Map<String, TranslationStorage> translations;

    public WikiTranslationManager() {
    }

    private static Set<String> decideLanguage() {
        var configLanguage = HeyWikiConfig.language;
        if (configLanguage.equals("auto")) {
            return getAllMainLanguages();
        } else {
            var mainLanguages = getAllMainLanguages();
            var defaultLanguages = getAllDefaultLanguagesFromWikiLanguage(configLanguage);
            mainLanguages.addAll(defaultLanguages);
            return mainLanguages;
        }
    }

    public static TranslationStorage loadTranslation(String language, ResourceManager resourceManager, boolean fallbackEnUs) {
        return language.equals("en_us") || !fallbackEnUs
                ? TranslationStorage.load(
                resourceManager,
                List.of(language), false)
                : TranslationStorage.load(
                resourceManager,
                List.of("en_us", language), false);
    }

    @Override
    public void reload(ResourceManager manager) {
        Map<String, TranslationStorage> translationsNew = new HashMap<>();
        for (String language : decideLanguage()) {
            translationsNew.put(language, loadTranslation(language, manager, true));
        }
        for (String language : getLangOverride()) {
            translationsNew.put(language, loadTranslation(language, manager, false));
        }

        translations = translationsNew;
    }
}
