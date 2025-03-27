package wiki.minecraft.heywiki.entrypoint;

import dev.architectury.event.CompoundEventResult;
import net.minecraft.network.message.MessageType;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.wiki.WikiFamily;
import wiki.minecraft.heywiki.wiki.WikiIndividual;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles wiki links in chat messages.
 */
public class ChatWikiLinks {
    private static final HeyWikiClient MOD = HeyWikiClient.getInstance();

    /**
     * Should be called at {@link dev.architectury.event.events.client.ClientChatEvent#RECEIVED ClientChatEvent#RECEIVED}.
     *
     * @param ignoredType The message type.
     * @param message     The message.
     * @return The new message.
     */
    public static CompoundEventResult<Text> onClientChatReceived(MessageType.Parameters ignoredType, Text message) {
        MutableText text = Text.empty();

        message.visit((style, string) -> {
            // noinspection RegExpRedundantEscape
            Pattern wikiLinkPattern = Pattern.compile("\\[\\[(.*?)\\]\\]");
            Matcher matcher = wikiLinkPattern.matcher(string);

            int lastEnd = 0;
            while (matcher.find()) {
                text.append(Text.literal(string.substring(lastEnd, matcher.start() + 2)).setStyle(style));

                String link = matcher.group(1);
                text.append(Text.literal(link).setStyle(
                        style
                                .withClickEvent(new ClickEvent.OpenUrl(linkToPage(link).getUri()))
                                .withUnderline(true)));

                lastEnd = matcher.end() - 2;
            }

            text.append(Text.literal(string.substring(lastEnd)).setStyle(style));

            return Optional.empty();
        }, Style.EMPTY);

        return CompoundEventResult.interruptTrue(text);
    }

    /**
     * Creates a wiki page from a wikitext link.
     *
     * @param link The wikitext link's target.
     * @return The wiki page.
     */
    public static WikiPage linkToPage(String link) {
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
}
