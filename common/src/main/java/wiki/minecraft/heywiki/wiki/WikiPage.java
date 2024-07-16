package wiki.minecraft.heywiki.wiki;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import wiki.minecraft.heywiki.HeyWikiConfig;
import wiki.minecraft.heywiki.gui.screen.HeyWikiConfirmLinkScreen;
import wiki.minecraft.heywiki.resource.WikiFamilyConfigManager;
import wiki.minecraft.heywiki.resource.WikiTranslationManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import static wiki.minecraft.heywiki.resource.WikiFamilyConfigManager.activeWikis;
import static wiki.minecraft.heywiki.resource.WikiTranslationManager.getTranslationOverride;

public record WikiPage(String pageName, WikiIndividual wiki) {
    public static final Text NO_FAMILY_MESSAGE = Text.translatable("heywiki.no_family")
                                                     .setStyle(Style.EMPTY.withColor(Formatting.RED));
    public static final SimpleCommandExceptionType NO_FAMILY_EXCEPTION = new SimpleCommandExceptionType(
            Text.translatable("heywiki.no_family"));
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static @Nullable WikiPage fromTarget(Target target) {
        return fromTarget(target.identifier(), target.translationKey());
    }

    public static @Nullable WikiPage fromTarget(Identifier identifier, String translationKey) {
        var family = WikiFamilyConfigManager.getFamilyByNamespace(identifier.getNamespace());
        if (family == null) return null;

        if (HeyWikiConfig.language.equals("auto")) {
            var language = client.options.language;
            var wiki = family.getLanguageWikiByGameLanguage(language);
            if (wiki != null) {
                TranslationStorage storage = getTranslationOverride(wiki);
                if (storage != null && storage.hasTranslation(translationKey)) {
                    String override = storage.get(translationKey, identifier.getPath());
                    return new WikiPage(override, wiki);
                }
                return new WikiPage(Language.getInstance().get(translationKey, identifier.getPath()), wiki);
            }
        } else {
            var language = HeyWikiConfig.language;
            var wiki = family.getLanguageWikiByWikiLanguage(language);
            if (wiki != null) {
                if (wiki.language().matchLanguage(client.options.language)) {
                    TranslationStorage storage = getTranslationOverride(wiki);
                    if (storage != null && storage.hasTranslation(translationKey)) {
                        String override = storage.get(translationKey, identifier.getPath());
                        return new WikiPage(override, wiki);
                    }
                    return new WikiPage(Language.getInstance().get(translationKey, identifier.getPath()), wiki);
                } else {
                    TranslationStorage storage = getTranslationOverride(wiki);
                    if (storage != null && storage.hasTranslation(translationKey)) {
                        String override = storage.get(translationKey, identifier.getPath());
                        return new WikiPage(override, wiki);
                    }
                    return new WikiPage(WikiTranslationManager.translations
                                                .get(wiki.language().defaultLanguage())
                                                .get(translationKey, identifier.getPath()), wiki);
                }
            }
        }

        WikiIndividual wiki = Objects.requireNonNull(family.getMainLanguageWiki());
        return new WikiPage(WikiTranslationManager.translations
                                    .get(wiki.language().defaultLanguage())
                                    .get(translationKey), wiki);
    }

    public static WikiPage fromWikitextLink(String link) {
        String[] split = link.split(":", 3);
        if (split.length == 1) {
            // [[Grass]]
            return new WikiPage(link, activeWikis.get("minecraft"));
        }

        WikiIndividual languageWiki = Objects.requireNonNull(WikiFamilyConfigManager.getFamilyByNamespace("minecraft"))
                                             .getLanguageWikiByWikiLanguage(split[0]);
        if (languageWiki != null) {
            // valid language: [[en:Grass]]
            return new WikiPage(link.split(":", 2)[1], languageWiki);
        }

        if (WikiFamilyConfigManager.getAvailableNamespaces().contains(split[0])) {
            // valid NS
            if (split.length == 3) {
                WikiFamily family = Objects.requireNonNull(WikiFamilyConfigManager.getFamilyByNamespace(split[0]));
                WikiIndividual languageWiki1 = family.getLanguageWikiByWikiLanguage(split[1]);
                if (languageWiki1 != null) {
                    // valid language: [[minecraft:en:Grass]]
                    return new WikiPage(split[2], languageWiki1);
                }
            }
            // invalid language: [[minecraft:Grass]]
            return new WikiPage(link.split(":", 2)[1], activeWikis.get(split[0]));
        }

        // [[Minecraft Legend:Grass]]
        return new WikiPage(link, activeWikis.get("minecraft"));
    }

    public static @Nullable WikiPage random(String namespace) {
        WikiIndividual wiki = activeWikis.get(namespace);
        if (wiki.randomArticle().isEmpty()) return null;
        return new WikiPage(wiki.randomArticle().get(), wiki);
    }

    public static @Nullable WikiPage versionArticle(String version) {
        var wiki = activeWikis.get("minecraft");
        if (wiki.versionArticle().isEmpty()) return null;
        Optional<String> name = wiki.versionArticle();
        return name.map(s -> new WikiPage(s.formatted(version), wiki)).orElse(null);
    }

    public void openInBrowser() {
        openInBrowser(false);
    }

    public void openInBrowser(Boolean skipConfirmation) {
        openInBrowser(skipConfirmation, null);
    }

    public void openInBrowser(Boolean skipConfirmation, Screen parent) {
        var uri = getUri();
        if (uri != null) {
            if (HeyWikiConfig.requiresConfirmation && !skipConfirmation) {
                HeyWikiConfirmLinkScreen.open(parent, uri.toString(), PageExcerpt.fromPage(this), this);
            } else {
                Util.getOperatingSystem().open(uri);
            }
        }
    }

    public @Nullable URI getUri() {
        try {
            return new URI(this.wiki.articleUrl().formatted(
                    URLEncoder.encode(this.wiki.title().formatTitle(this.pageName), StandardCharsets.UTF_8)));
        } catch (URISyntaxException e) {
            LOGGER.error("Failed to create URI for wiki page", e);
            return null;
        }
    }
}
