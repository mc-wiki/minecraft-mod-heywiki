package wiki.minecraft.heywiki.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import net.minecraft.client.MinecraftClient;
import wiki.minecraft.heywiki.WikiPage;

import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.literal;

public class WhatBiomeCommand {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    @SuppressWarnings("UnusedReturnValue")
    public static LiteralCommandNode<ClientCommandSourceStack> register(CommandDispatcher<ClientCommandSourceStack> dispatcher) {
        return dispatcher.register(literal("whatbiome")
                .executes(ctx -> {
                    if (CLIENT.player == null || CLIENT.world == null) return 1;

                    var block = CLIENT.player.getBlockPos();
                    var biomeKey = CLIENT.world.getBiome(block).getKey();
                    if (biomeKey.isEmpty()) return 1;
                    String translationKey = biomeKey.get().getValue().toTranslationKey("biome");
                    WikiPage.fromTranslationKey(translationKey).openInBrowser(true);
                    return 0;
                }));
    }
}