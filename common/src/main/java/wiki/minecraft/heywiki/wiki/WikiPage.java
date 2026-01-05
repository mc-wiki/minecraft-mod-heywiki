package wiki.minecraft.heywiki.wiki;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.gui.screen.ConfirmWikiPageScreen;
import wiki.minecraft.heywiki.target.Target;

import java.net.URI;
import java.net.URISyntaxException;
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
    public static final Component NO_FAMILY_MESSAGE = Component.translatable("gui.heywiki.no_family")
                                                               .setStyle(Style.EMPTY.withColor(ChatFormatting.RED));
    /**
     * The exception to throw when no family is found in a command.
     */
    public static final SimpleCommandExceptionType NO_FAMILY_EXCEPTION =
            new SimpleCommandExceptionType(Component.translatable("gui.heywiki.no_family"));
    private static final HeyWikiClient MOD = HeyWikiClient.getInstance();

    /**
     * Creates a wiki page from a target.
     *
     * @param target The target.
     * @return The wiki page.
     */
    public static @Nullable WikiPage fromTarget(@NotNull Target target) {
        WikiIndividual wiki = MOD.familyManager().activeWikis().get(target.namespace());
        if (wiki == null) return null;
        return new WikiPage(target.title(), wiki);
    }

    /**
     * Gets a random article from the given namespace.
     *
     * @param namespace The namespace.
     * @return The random article.
     */
    public static @Nullable WikiPage random(String namespace) {
        WikiIndividual wiki = MOD.familyManager().activeWikis().get(namespace);
        if (wiki == null) return null;
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
     * Open the page in the browser.
     *
     * @param requiresConfirmation Whether to skip the confirmation dialog.
     * @param parent               The parent screen.
     */
    public void openInBrowser(Boolean requiresConfirmation, Screen parent) {
        var uri = getUri();
        if (requiresConfirmation) {
            ConfirmWikiPageScreen.open(parent, uri.toString(), PageExcerpt.fromPage(this), this);
        } else {
            Util.getPlatform().openUri(uri);
        }
    }

    /**
     * Gets the URI of the page.
     *
     * @return The URI.
     */
    public URI getUri() {
        try {
            return new URI(this.wiki.articleUrl().formatted(encodeUrl(this.wiki.title().formatTitle(this.pageName))));
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to create URI for wiki page", e);
        }
    }

    /**
     * Open the page in the browser, while displaying a confirmation dialog based on global configuration.
     *
     * @param parent The parent screen.
     */
    public void openInBrowser(Screen parent) {
        openInBrowser(MOD.config().requiresConfirmation(),
                      parent == null ? Minecraft.getInstance().screen : parent);
    }
}
