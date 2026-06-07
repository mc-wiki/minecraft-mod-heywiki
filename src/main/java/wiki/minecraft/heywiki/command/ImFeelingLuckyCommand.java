package wiki.minecraft.heywiki.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import wiki.minecraft.heywiki.command.suggestion.NamespaceSuggestionProvider;
import wiki.minecraft.heywiki.wiki.WikiPage;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static wiki.minecraft.heywiki.platform.HeyWikiPlatform.argument;
import static wiki.minecraft.heywiki.platform.HeyWikiPlatform.literal;

public class ImFeelingLuckyCommand {
    public static final SimpleCommandExceptionType NOT_SUPPORTED = new SimpleCommandExceptionType(
            Component.translatable("commands.imfeelinglucky.not_supported"));

    @SuppressWarnings("UnusedReturnValue")
    public static LiteralCommandNode<SharedSuggestionProvider> register(
            CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        return dispatcher.register(
                literal("imfeelinglucky")
                        .executes(ctx -> openRandomPage("minecraft"))
                        .then(argument("namespace", string())
                                      .suggests(new NamespaceSuggestionProvider(false))
                                      .executes(ctx -> openRandomPage(getString(ctx, "namespace")))));
    }

    private static int openRandomPage(String namespace) throws CommandSyntaxException {
        WikiPage randomPage = WikiPage.random(namespace);
        if (randomPage == null) throw NOT_SUPPORTED.create();

        randomPage.openInBrowserCommand(null);

        return Command.SINGLE_SUCCESS;
    }
}