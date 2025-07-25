package wiki.minecraft.heywiki.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import wiki.minecraft.heywiki.target.Target;
import wiki.minecraft.heywiki.wiki.WikiPage;

import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.literal;
import static wiki.minecraft.heywiki.wiki.WikiPage.NO_FAMILY_EXCEPTION;

public class WhatIsThisItemCommand {
    public static final SimpleCommandExceptionType NO_ITEM_HELD = new SimpleCommandExceptionType(
            Component.translatable("gui.heywiki.no_item_held"));
    private static final Minecraft CLIENT = Minecraft.getInstance();

    @SuppressWarnings("UnusedReturnValue")
    public static LiteralCommandNode<ClientCommandSourceStack> register(
            CommandDispatcher<ClientCommandSourceStack> dispatcher) {
        assert CLIENT.player == null;
        return dispatcher.register(
                literal("whatisthisitem")
                        .executes(ctx -> openBrowserForStack(CLIENT.player.getInventory().getSelectedItem()))
                        .then(literal("offhand")
                                      .executes(ctx -> openBrowserForStack(
                                              CLIENT.player.getInventory().getItem(Inventory.SLOT_OFFHAND)))));
    }

    private static int openBrowserForStack(ItemStack stack) throws CommandSyntaxException {
        var target = Target.of(stack);
        if (target == null) {
            throw NO_ITEM_HELD.create();
        }
        var page = WikiPage.fromTarget(target);
        if (page == null) {
            throw NO_FAMILY_EXCEPTION.create();
        }
        page.openInBrowserCommand(null);
        return Command.SINGLE_SUCCESS;
    }
}