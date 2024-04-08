package wiki.minecraft.heywiki.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import wiki.minecraft.heywiki.command.suggestion.NamespaceSuggestionProvider;
import wiki.minecraft.heywiki.resource.WikiFamilyConfigManager;
import wiki.minecraft.heywiki.wiki.WikiPage;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.argument;
import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.literal;

public class ImFeelingLuckyCommand {
    @SuppressWarnings("UnusedReturnValue")
    public static LiteralCommandNode<ClientCommandSourceStack> register(CommandDispatcher<ClientCommandSourceStack> dispatcher) {
        return dispatcher.register(literal("imfeelinglucky")
                .executes(ctx -> {
                    WikiPage.random(WikiFamilyConfigManager.getFamilyByNamespace("minecraft")).openInBrowser(true);
                    return 0;
                })
                .then(argument("namespace", string())
                        .suggests(new NamespaceSuggestionProvider(false))
                        .executes(ctx -> {
                            String namespace = getString(ctx, "namespace");
                            WikiPage.random(WikiFamilyConfigManager.getFamilyByNamespace(namespace)).openInBrowser(true);
                            return 0;
                        })));
    }
}