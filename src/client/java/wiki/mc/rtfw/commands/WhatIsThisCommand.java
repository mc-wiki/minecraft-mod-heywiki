package wiki.mc.rtfw.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import wiki.mc.rtfw.WikiPage;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static wiki.mc.rtfw.RTFWClient.getTranslationKeyByRaycast;

public class WhatIsThisCommand {
    public static final SimpleCommandExceptionType NO_TARGET = new SimpleCommandExceptionType(Text.translatable("commands.whatisthis.no_target"));

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("whatisthis")
                .executes(ctx -> {
                    String translationKey = getTranslationKeyByRaycast();
                    if (translationKey == null) throw NO_TARGET.create();
                    WikiPage.fromTranslationKey(translationKey).openInBrowser(true);
                    return 0;
                }));
    }
}