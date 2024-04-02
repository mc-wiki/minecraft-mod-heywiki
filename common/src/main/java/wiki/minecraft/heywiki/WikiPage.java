package wiki.minecraft.heywiki;

import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import wiki.minecraft.heywiki.screen.HeyWikiConfirmLinkScreen;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

public class WikiPage {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static String currentLang;
    private static TranslationStorage translation;

    private static HashMap<String, String> getWikiLanguageGameLanguageMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("de", "de_de");
        map.put("en", "en_us");
        map.put("es", "es_es");
        map.put("fr", "fr_fr");
        map.put("ja", "ja_jp");
        map.put("ko", "ko_kr");
        map.put("lzh", "lzh");
        map.put("pt", "pt_br");
        map.put("ru", "ru_ru");
        map.put("th", "th_th");
        map.put("uk", "uk_ua");
        map.put("zh", "zh_cn");
        return map;
    }

    String pageName;

    public WikiPage(String pageName) {
        this.pageName = pageName;
    }

    public static void loadTranslation(String language) {
        currentLang = language;
        translation = language.equals("en_us")
                ? TranslationStorage.load(
                client.getResourceManager(),
                List.of("en_us", language), false)
                : TranslationStorage.load(
                client.getResourceManager(),
                List.of("en_us"), false);
    }

    public static WikiPage fromTranslationKey(String translationKey) {
        if (HeyWikiConfig.language.equals("auto")) {
            String resolvedLanguage = resolveWikiLanguage(client.options.language);
            if (!resolvedLanguage.equals("en")) {
                return new WikiPage(I18n.translate(translationKey));
            }

            if (!resolvedLanguage.equals(currentLang)) {
                loadTranslation("en_us");
            }
            return new WikiPage(translation.get(translationKey));
        } else {
            String mappedConfigLanguage = getWikiLanguageGameLanguageMap().get(HeyWikiConfig.language);
            if (!mappedConfigLanguage.equals(currentLang)) {
                loadTranslation(mappedConfigLanguage);
            }

            return new WikiPage(translation.get(translationKey));
        }
    }

    public static String resolveWikiLanguage(String language) {
        if (language.startsWith("de_")) {
            return "de";
        } else if (language.startsWith("es_")) {
            return "es";
        } else if (language.startsWith("fr_")) {
            return "fr";
        } else if (language.startsWith("ja_")) {
            return "ja";
        } else if (language.equals("lzh")) {
            return "lzh";
        } else if (language.startsWith("ko_")) {
            return "ko";
        } else if (language.startsWith("pt_")) {
            return "pt";
        } else if (language.startsWith("ru_")) {
            return "ru";
        } else if (language.startsWith("th_")) {
            return "th";
        } else if (language.startsWith("uk_")) {
            return "uk";
        } else if (language.startsWith("zh_")) {
            return "zh";
        }
        return "en";
    }

    public @Nullable URI getUri() {
        String resolvedLanguage = HeyWikiConfig.language.equals("auto") ? resolveWikiLanguage(client.options.language) : HeyWikiConfig.language;

        try {
            if (resolvedLanguage.equals("en"))
                return new URI("https://minecraft.wiki/?search=" + URLEncoder.encode(this.pageName.replaceAll(" ", "_"), StandardCharsets.UTF_8));
            else
                return new URI("https://" + resolvedLanguage + ".minecraft.wiki/?search=" + URLEncoder.encode(this.pageName.replaceAll(" ", "_"), StandardCharsets.UTF_8));
        } catch (URISyntaxException e) {
            LOGGER.trace("Failed to create URI for wiki page", e);
        }

        return null;
    }

    public void openInBrowser() {
        openInBrowser(false);
    }

    public void openInBrowser(Boolean skipConfirmation) {
        var uri = getUri();
        if (uri != null) {
            if (HeyWikiConfig.requiresConfirmation && !skipConfirmation) {
                HeyWikiConfirmLinkScreen.open(null, uri.toString());
            } else {
                Util.getOperatingSystem().open(uri);
            }
        }
    }
}
