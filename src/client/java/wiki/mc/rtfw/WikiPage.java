package wiki.mc.rtfw;

import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WikiPage {
    String pageName;

    public WikiPage(String pageName) {
        this.pageName = pageName;
    }

    public @Nullable URI buildUri(String language) {
        String solvedLanguage = null;
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

        try {
            if (solvedLanguage == null)
                return new URI("https://minecraft.wiki/w/" + URLEncoder.encode(this.pageName.replaceAll(" ", "_"), StandardCharsets.UTF_8));
            else
                return new URI("https://" + solvedLanguage + ".minecraft.wiki/w/" + URLEncoder.encode(this.pageName.replaceAll(" ", "_"), StandardCharsets.UTF_8));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void openInBrowser(String language) {
        var uri = buildUri(language);
        if (uri != null) {
            Util.getOperatingSystem().open(uri);
        }
    }
}
