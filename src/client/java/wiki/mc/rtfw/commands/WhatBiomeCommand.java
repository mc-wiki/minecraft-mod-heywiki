package wiki.mc.rtfw.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import wiki.mc.rtfw.WikiPage;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class WhatBiomeCommand {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("whatbiome")
                .executes(ctx -> {
                    // TODO: Throw out errors
                    if (client.player == null || client.world == null) return 1;

                    var block = client.player.getBlockPos();
                    var biomeKey = client.world.getBiome(block).getKey();
                    if (biomeKey.isEmpty()) return 1;
                    String translationKey = biomeKey.get().getValue().toTranslationKey("biome");
                    WikiPage.fromTranslationKey(translationKey).openInBrowser(true);
                    return 0;
                }));
    }
}