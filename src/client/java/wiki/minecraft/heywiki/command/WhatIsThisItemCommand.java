package wiki.minecraft.heywiki.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import wiki.minecraft.heywiki.WikiPage;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class WhatIsThisItemCommand {
    public static final SimpleCommandExceptionType NO_ITEM_HELD = new SimpleCommandExceptionType(Text.translatable("commands.whatisthisitem.no_item_held"));
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("whatisthisitem")
                .executes(ctx -> {
                    if (CLIENT.player == null) return 1;
                    ItemStack stack = CLIENT.player.getInventory().getMainHandStack();
                    if (stack.isEmpty()) {
                        throw NO_ITEM_HELD.create();
                    }
                    String translationKey = stack.getItem().getTranslationKey();
                    WikiPage.fromTranslationKey(translationKey).openInBrowser(true);
                    return 0;
                }));
    }
}