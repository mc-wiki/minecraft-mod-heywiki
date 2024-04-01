package wiki.minecraft.heywiki.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import wiki.minecraft.heywiki.WikiPage;
import wiki.minecraft.heywiki.command.suggestion.CommandNameSuggestionProvider;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class WhatCommandCommand {
    @SuppressWarnings("UnusedReturnValue")
    public static LiteralCommandNode<FabricClientCommandSource> register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        return dispatcher.register(literal("whatcommand")
                .then(argument("command", string())
                        .suggests(new CommandNameSuggestionProvider())
                        .executes(ctx -> {
                            new WikiPage("/" + getString(ctx, "command")).openInBrowser(true);
                            return 0;
                        })));
    }
}