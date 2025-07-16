package wiki.minecraft.heywiki.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import net.minecraft.SharedConstants;
import wiki.minecraft.heywiki.wiki.WikiPage;

import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.literal;

public class WhatVersionCommand {
    @SuppressWarnings("UnusedReturnValue")
    public static LiteralCommandNode<ClientCommandSourceStack> register(
            CommandDispatcher<ClientCommandSourceStack> dispatcher) {
        return dispatcher.register(literal("whatversion").executes(ctx -> {
            var article = WikiPage.versionArticle(SharedConstants.getCurrentVersion().name());
            if (article != null) {
                article.openInBrowserCommand(null);
            }
            return Command.SINGLE_SUCCESS;
        }));
    }
}