package wiki.minecraft.heywiki.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import net.minecraft.text.Text;
import wiki.minecraft.heywiki.Raycast;
import wiki.minecraft.heywiki.target.Target;
import wiki.minecraft.heywiki.wiki.WikiPage;

import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.literal;
import static wiki.minecraft.heywiki.wiki.WikiPage.NO_FAMILY_EXCEPTION;

public class WhatIsThisCommand {
    public static final SimpleCommandExceptionType NO_TARGET = new SimpleCommandExceptionType(
            Text.translatable("gui.heywiki.too_far"));

    @SuppressWarnings("UnusedReturnValue")
    public static LiteralCommandNode<ClientCommandSourceStack> register(
            CommandDispatcher<ClientCommandSourceStack> dispatcher) {
        return dispatcher.register(literal("whatisthis").executes(ctx -> {
            Target target = Raycast.raycast();
            if (target == null) throw NO_TARGET.create();
            var page = WikiPage.fromTarget(target);
            if (page == null) {
                throw NO_FAMILY_EXCEPTION.create();
            }
            page.openInBrowserCommand(null);
            return 0;
        }));
    }
}