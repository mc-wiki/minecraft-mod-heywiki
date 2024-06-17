package wiki.minecraft.heywiki.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import wiki.minecraft.heywiki.command.suggestion.CommandNameSuggestionProvider;
import wiki.minecraft.heywiki.wiki.WikiPage;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.argument;
import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.literal;
import static wiki.minecraft.heywiki.resource.WikiFamilyConfigManager.activeWikis;

public class WhatCommandCommand {
    @SuppressWarnings("UnusedReturnValue")
    public static LiteralCommandNode<ClientCommandSourceStack> register(CommandDispatcher<ClientCommandSourceStack> dispatcher) {
        return dispatcher.register(literal("whatcommand")
                .then(argument("command", string())
                        .suggests(new CommandNameSuggestionProvider())
                        .executes(ctx -> {
                            // Unfortunately, I don't think we can check where the command comes from
                            new WikiPage("/" + getString(ctx, "command"), activeWikis.get("minecraft")).openInBrowser(true);
                            return 0;
                        })));
    }
}