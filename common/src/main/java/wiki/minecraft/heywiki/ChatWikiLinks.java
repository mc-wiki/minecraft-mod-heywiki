package wiki.minecraft.heywiki;

import dev.architectury.event.CompoundEventResult;
import net.minecraft.network.message.MessageType;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.text.ClickEvent.Action.OPEN_URL;

public class ChatWikiLinks {
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
                                .withClickEvent(new ClickEvent(OPEN_URL, Objects.requireNonNull(WikiPage.fromWikitextLink(link).getUri()).toString()))
                                .withUnderline(true)));

                lastEnd = matcher.end() - 2;
            }

            text.append(Text.literal(string.substring(lastEnd)).setStyle(style));

            return Optional.empty();
        }, Style.EMPTY);

        return CompoundEventResult.interruptTrue(text);
    }
}
