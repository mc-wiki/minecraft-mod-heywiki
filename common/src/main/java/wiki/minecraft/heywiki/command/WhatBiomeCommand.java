package wiki.minecraft.heywiki.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import net.minecraft.client.MinecraftClient;
import wiki.minecraft.heywiki.wiki.Target;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.util.Objects;

import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.literal;

public class WhatBiomeCommand {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    @SuppressWarnings("UnusedReturnValue")
    public static LiteralCommandNode<ClientCommandSourceStack> register(CommandDispatcher<ClientCommandSourceStack> dispatcher) {
        return dispatcher.register(literal("whatbiome")
                .executes(ctx -> {
                    if (CLIENT.player == null || CLIENT.world == null) return 1;

                    var block = CLIENT.player.getBlockPos();
                    var biomeRegistryEntry = CLIENT.world.getBiome(block);
                    var target = Target.of(biomeRegistryEntry);
                    if (target == null) return 1;
                    Objects.requireNonNull(WikiPage.fromTarget(target)).openInBrowser(true);
                    return 0;
                }));
    }
}