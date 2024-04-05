package wiki.minecraft.heywiki.wiki;

import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import wiki.minecraft.heywiki.HeyWikiConfig;
import wiki.minecraft.heywiki.resource.WikiFamilyConfigManager;
import wiki.minecraft.heywiki.resource.WikiTranslationManager;
import wiki.minecraft.heywiki.screen.HeyWikiConfirmLinkScreen;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class WikiPage {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final MinecraftClient client = MinecraftClient.getInstance();
    String pageName;
    WikiFamily family;

    public WikiPage(String pageName, WikiFamily family) {
        this.pageName = pageName;
        this.family = family;
    }

    public static @Nullable WikiPage fromIdentifier(IdentifierTranslationKey identifierTranslationKey) {
        return fromIdentifier(identifierTranslationKey.identifier, identifierTranslationKey.translationKey);
    }

    public static @Nullable WikiPage fromIdentifier(Identifier identifier, String translationKey) {
        var family = WikiFamilyConfigManager.getFamilyByNamespace(identifier.getNamespace());

        if (family == null) return null;

        if (HeyWikiConfig.language.equals("auto")) {
            var language = client.options.language;
            var wiki = family.getLanguageWikiByGameLanguage(language);
            if (wiki != null) {
                return new WikiPage(I18n.translate(translationKey), family);
            }
        } else {
            var language = HeyWikiConfig.language;
            var wiki = family.getLanguageWikiByWikiLanguage(language);
            if (wiki != null) {
                if (wiki.language.matchLanguage(client.options.language)) {
                    return new WikiPage(I18n.translate(translationKey), family);
                } else {
                    return new WikiPage(WikiTranslationManager.translations
                            .get(wiki.language.defaultLanguage)
                            .get(translationKey), family);
                }
            }
        }

        return new WikiPage(WikiTranslationManager.translations
                .get(Objects.requireNonNull(family.getMainLanguageWiki()).language.defaultLanguage)
                .get(translationKey), family);
    }

    public @Nullable URI getUri() {
        WikiFamily.IndividualWiki wiki;

        if (HeyWikiConfig.language.equals("auto")) {
            var language = client.options.language;
            wiki = family.getLanguageWikiByGameLanguage(language);
        } else {
            var language = HeyWikiConfig.language;
            wiki = family.getLanguageWikiByWikiLanguage(language);
        }

        if (wiki == null) wiki = family.getLanguageWikiByGameLanguage("en_us");

        if (wiki == null) {
            LOGGER.error("Failed to find wiki for language {}", HeyWikiConfig.language);
            return null;
        }

        try {
            return new URI(wiki.urlPattern.formatted(URLEncoder.encode(this.pageName.replaceAll(" ", "_"), StandardCharsets.UTF_8)));
        } catch (URISyntaxException e) {
            LOGGER.error("Failed to create URI for wiki page", e);
            return null;
        }
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
