package wiki.minecraft.heywiki.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.util.Objects;

import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.literal;

public class WhatIsThisItemCommand {
    public static final SimpleCommandExceptionType NO_ITEM_HELD = new SimpleCommandExceptionType(Text.translatable("commands.whatisthisitem.no_item_held"));
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    @SuppressWarnings("UnusedReturnValue")
    public static LiteralCommandNode<ClientCommandSourceStack> register(CommandDispatcher<ClientCommandSourceStack> dispatcher) {
        return dispatcher.register(literal("whatisthisitem")
                .executes(ctx -> {
                    if (CLIENT.player == null) return 1;
                    ItemStack stack = CLIENT.player.getInventory().getMainHandStack();
                    if (stack.isEmpty()) {
                        throw NO_ITEM_HELD.create();
                    }
                    String translationKey = stack.getTranslationKey();
                    Identifier identifier = stack.getItem().arch$registryName();
                    if (identifier != null) {
                        Objects.requireNonNull(WikiPage.fromIdentifier(identifier, translationKey)).openInBrowser(true);
                    }
                    return 0;
                }).then(literal("offhand")
                        .executes(ctx -> {
                            if (CLIENT.player == null) return 1;
                            ItemStack stack = CLIENT.player.getInventory().offHand.get(0);
                            if (stack.isEmpty()) {
                                throw NO_ITEM_HELD.create();
                            }
                            String translationKey = stack.getTranslationKey();
                            Identifier identifier = stack.getItem().arch$registryName();
                            if (identifier != null) {
                                Objects.requireNonNull(WikiPage.fromIdentifier(identifier, translationKey)).openInBrowser(true);
                            }
                            return 0;
                        })));
    }
}