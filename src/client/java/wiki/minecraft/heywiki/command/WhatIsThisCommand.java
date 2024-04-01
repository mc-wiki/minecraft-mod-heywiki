package wiki.minecraft.heywiki.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import wiki.minecraft.heywiki.WikiPage;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static wiki.minecraft.heywiki.HeyWikiClient.getTranslationKeyByRaycast;

public class WhatIsThisCommand {
    public static final SimpleCommandExceptionType NO_TARGET = new SimpleCommandExceptionType(Text.translatable("commands.whatisthis.no_target"));

    public static LiteralCommandNode<FabricClientCommandSource> register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        return dispatcher.register(literal("whatisthis")
                .executes(ctx -> {
                    String translationKey = getTranslationKeyByRaycast();
                    if (translationKey == null) throw NO_TARGET.create();
                    WikiPage.fromTranslationKey(translationKey).openInBrowser(true);
                    return 0;
                }));
    }
}