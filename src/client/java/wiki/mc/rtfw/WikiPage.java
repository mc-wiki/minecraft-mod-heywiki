package wiki.mc.rtfw;

import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

public class WikiPage {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final RTFWConfig config = RTFWConfig.HANDLER.instance();
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final HashMap<String, TranslationStorage> languageMap = new HashMap<>();

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

    public static WikiPage fromTranslationKey(String translationKey) {
        if (config.language.equals("auto")) {
            return new WikiPage(Text.translatable(translationKey).getString());
        }
        String mappedConfigLanguage = getWikiLanguageGameLanguageMap().get(config.language);
        if (!languageMap.containsKey(mappedConfigLanguage)) {
            languageMap.put(mappedConfigLanguage, TranslationStorage.load(
                    client.getResourceManager(),
                    Arrays.asList(mappedConfigLanguage, "en_us"), false));
        }

        return new WikiPage(languageMap.get(mappedConfigLanguage).get(translationKey));
    }

    public @Nullable URI getUri() {
        String solvedLanguage = "en";

        if (config.language.equals("auto")) {
            var language = client.options.language;

            if (language.startsWith("de_")) {
                solvedLanguage = "de";
            } else if (language.startsWith("es_")) {
                solvedLanguage = "es";
            } else if (language.startsWith("fr_")) {
                solvedLanguage = "fr";
            } else if (language.startsWith("ja_")) {
                solvedLanguage = "ja";
            } else if (language.equals("lzh")) {
                solvedLanguage = "lzh";
            } else if (language.startsWith("ko_")) {
                solvedLanguage = "ko";
            } else if (language.startsWith("pt_")) {
                solvedLanguage = "pt";
            } else if (language.startsWith("ru_")) {
                solvedLanguage = "ru";
            } else if (language.startsWith("th_")) {
                solvedLanguage = "th";
            } else if (language.startsWith("uk_")) {
                solvedLanguage = "uk";
            } else if (language.startsWith("zh_")) {
                solvedLanguage = "zh";
            }
        } else {
            solvedLanguage = config.language;
        }

        try {
            if (solvedLanguage.equals("en"))
                return new URI("https://minecraft.wiki/?search=" + URLEncoder.encode(this.pageName.replaceAll(" ", "_"), StandardCharsets.UTF_8));
            else
                return new URI("https://" + solvedLanguage + ".minecraft.wiki/?search=" + URLEncoder.encode(this.pageName.replaceAll(" ", "_"), StandardCharsets.UTF_8));
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
            if (config.requiresConfirmation && !skipConfirmation) {
                RTFWConfirmLinkScreen.open(null, uri.toString());
            } else {
                Util.getOperatingSystem().open(uri);
            }
        }
    }
}
