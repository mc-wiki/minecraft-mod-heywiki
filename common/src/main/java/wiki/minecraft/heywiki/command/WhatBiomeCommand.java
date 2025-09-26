package wiki.minecraft.heywiki.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import net.minecraft.client.Minecraft;
import wiki.minecraft.heywiki.target.Target;
import wiki.minecraft.heywiki.wiki.WikiPage;

import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.literal;
import static wiki.minecraft.heywiki.wiki.WikiPage.NO_FAMILY_EXCEPTION;

public class WhatBiomeCommand {
    private static final Minecraft CLIENT = Minecraft.getInstance();

    @SuppressWarnings("UnusedReturnValue")
    public static LiteralCommandNode<ClientCommandSourceStack> register(
            CommandDispatcher<ClientCommandSourceStack> dispatcher) {
        return dispatcher.register(
                literal("whatbiome")
                        .executes(ctx -> {
                            if (CLIENT.player == null || CLIENT.level == null) return -1;

                            var block = CLIENT.player.getOnPos();
                            var biomeRegistryEntry = CLIENT.level.getBiome(block);
                            var target = Target.of(biomeRegistryEntry, "biome");
                            if (target == null) return -1;
                            var page = WikiPage.fromTarget(target);
                            if (page == null) {
                                throw NO_FAMILY_EXCEPTION.create();
                            }
                            page.openInBrowserCommand(null);
                            return Command.SINGLE_SUCCESS;
                        }));
    }
}