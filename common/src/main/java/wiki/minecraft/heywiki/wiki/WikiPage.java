package wiki.minecraft.heywiki.wiki;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.gui.screen.ConfirmWikiPageScreen;
import wiki.minecraft.heywiki.target.Target;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;

import static wiki.minecraft.heywiki.util.HttpUtil.encodeUrl;

/**
 * Represents a wiki page.
 *
 * @param pageName The name of the page.
 * @param wiki     The wiki the page belongs to.
 * @see WikiIndividual
 * @see WikiFamily
 */
public record WikiPage(String pageName, WikiIndividual wiki) {
    /**
     * The message to display when no family is found.
     */
    public static final Text NO_FAMILY_MESSAGE = Text.translatable("gui.heywiki.no_family")
                                                     .setStyle(Style.EMPTY.withColor(Formatting.RED));
    /**
     * The exception to throw when no family is found in a command.
     */
    public static final SimpleCommandExceptionType NO_FAMILY_EXCEPTION = new SimpleCommandExceptionType(
            Text.translatable("gui.heywiki.no_family"));
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final HeyWikiClient MOD = HeyWikiClient.getInstance();

    /**
     * Creates a wiki page from a target.
     *
     * @param target The target.
     * @return The wiki page.
     */
    public static @Nullable WikiPage fromTarget(Target target) {
        if (target == null) return null;

        WikiIndividual wiki = MOD.familyManager().activeWikis().get(target.namespace());
        if (wiki == null) return null;
        return new WikiPage(target.title(), wiki);
    }

    /**
     * Creates a wiki page from a wikitext link.
     *
     * @param link The wikitext link's target.
     * @return The wiki page.
     */
    public static WikiPage fromWikitextLink(String link) {
        String[] split = link.split(":", 3);
        if (split.length == 1) {
            // [[Grass]]
            return new WikiPage(link, MOD.familyManager().activeWikis().get("minecraft"));
        }

        WikiIndividual languageWiki = Objects.requireNonNull(
                                                     MOD.familyManager().getFamilyByNamespace("minecraft"))
                                             .getLanguageWikiByWikiLanguage(split[0]);
        if (languageWiki != null) {
            // valid language: [[en:Grass]]
            return new WikiPage(link.split(":", 2)[1], languageWiki);
        }

        if (MOD.familyManager().getAvailableNamespaces().contains(split[0])) {
            // valid NS
            if (split.length == 3) {
                WikiFamily family = Objects.requireNonNull(
                        MOD.familyManager().getFamilyByNamespace(split[0]));
                WikiIndividual languageWiki1 = family.getLanguageWikiByWikiLanguage(split[1]);
                if (languageWiki1 != null) {
                    // valid language: [[minecraft:en:Grass]]
                    return new WikiPage(split[2], languageWiki1);
                }
            }
            // invalid language: [[minecraft:Grass]]
            return new WikiPage(link.split(":", 2)[1], MOD.familyManager().activeWikis().get(split[0]));
        }

        // [[Minecraft Legend:Grass]]
        return new WikiPage(link, MOD.familyManager().activeWikis().get("minecraft"));
    }

    /**
     * Gets a random article from the given namespace.
     *
     * @param namespace The namespace.
     * @return The random article.
     */
    public static @Nullable WikiPage random(String namespace) {
        WikiIndividual wiki = MOD.familyManager().activeWikis().get(namespace);
        if (wiki.randomArticle().isEmpty()) return null;
        return new WikiPage(wiki.randomArticle().get(), wiki);
    }

    /**
     * Gets the version article.
     *
     * @return The version article.
     */
    public static @Nullable WikiPage versionArticle(String version) {
        @Nullable var wiki = MOD.familyManager().activeWikis().get("minecraft");
        if (wiki == null || wiki.versionArticle().isEmpty()) return null;
        Optional<String> name = wiki.versionArticle();
        return name.map(s -> new WikiPage(s.formatted(version), wiki)).orElse(null);
    }

    /**
     * Open the page in the browser, while displaying a confirmation dialog based on command-specific configuration.
     *
     * @param parent The parent screen.
     */
    public void openInBrowserCommand(Screen parent) {
        openInBrowser(MOD.config().requiresConfirmationCommand(), parent);
    }

    /**
     * Open the page in the browser, while displaying a confirmation dialog based on global configuration.
     *
     * @param parent The parent screen.
     */
    public void openInBrowser(Screen parent) {
        openInBrowser(MOD.config().requiresConfirmation(), parent == null ? MinecraftClient.getInstance().currentScreen : parent);
    }

    /**
     * Open the page in the browser.
     *
     * @param requiresConfirmation Whether to skip the confirmation dialog.
     * @param parent               The parent screen.
     */
    public void openInBrowser(Boolean requiresConfirmation, Screen parent) {
        var uri = getUri();
        if (uri != null) {
            if (requiresConfirmation) {
                ConfirmWikiPageScreen.open(parent, uri.toString(), PageExcerpt.fromPage(this), this);
            } else {
                Util.getOperatingSystem().open(uri);
            }
        }
    }

    /**
     * Gets the URI of the page.
     *
     * @return The URI.
     */
    public @Nullable URI getUri() {
        try {
            return new URI(this.wiki.articleUrl().formatted(
                    encodeUrl(this.wiki.title().formatTitle(this.pageName))));
        } catch (URISyntaxException e) {
            LOGGER.error("Failed to create URI for wiki page", e);
            return null;
        }
    }
}
