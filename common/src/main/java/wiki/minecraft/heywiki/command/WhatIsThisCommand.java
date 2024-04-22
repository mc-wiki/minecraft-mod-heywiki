package wiki.minecraft.heywiki.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import net.minecraft.text.Text;
import wiki.minecraft.heywiki.wiki.Target;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.util.Objects;

import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.literal;
import static wiki.minecraft.heywiki.CrosshairRaycast.raycast;

public class WhatIsThisCommand {
    public static final SimpleCommandExceptionType NO_TARGET = new SimpleCommandExceptionType(Text.translatable("heywiki.too_far"));

    @SuppressWarnings("UnusedReturnValue")
    public static LiteralCommandNode<ClientCommandSourceStack> register(CommandDispatcher<ClientCommandSourceStack> dispatcher) {
        return dispatcher.register(literal("whatisthis").executes(ctx -> {
            Target target = raycast();
            if (target == null) throw NO_TARGET.create();
            Objects.requireNonNull(WikiPage.fromTarget(target)).openInBrowser(true);
            return 0;
        }));
    }
}