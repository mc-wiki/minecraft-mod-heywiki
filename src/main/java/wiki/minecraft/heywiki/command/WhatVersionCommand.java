package wiki.minecraft.heywiki.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.SharedConstants;
import net.minecraft.commands.SharedSuggestionProvider;
import wiki.minecraft.heywiki.wiki.WikiPage;

import static wiki.minecraft.heywiki.platform.HeyWikiPlatform.literal;

public class WhatVersionCommand {
    @SuppressWarnings("UnusedReturnValue")
    public static LiteralCommandNode<SharedSuggestionProvider> register(
            CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        return dispatcher.register(literal("whatversion").executes(ctx -> {
            var article = WikiPage.versionArticle(SharedConstants.getCurrentVersion().name());
            if (article != null) {
                article.openInBrowserCommand(null);
            }
            return Command.SINGLE_SUCCESS;
        }));
    }
}