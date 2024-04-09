package wiki.minecraft.heywiki.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import net.minecraft.text.Text;
import wiki.minecraft.heywiki.wiki.IdentifierTranslationKey;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.util.Objects;

import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.literal;
import static wiki.minecraft.heywiki.Raycast.getIdentifierByRaycast;

public class WhatIsThisCommand {
    public static final SimpleCommandExceptionType NO_TARGET = new SimpleCommandExceptionType(Text.translatable("commands.whatisthis.no_target"));

    @SuppressWarnings("UnusedReturnValue")
    public static LiteralCommandNode<ClientCommandSourceStack> register(CommandDispatcher<ClientCommandSourceStack> dispatcher) {
        return dispatcher.register(literal("whatisthis").executes(ctx -> {
            IdentifierTranslationKey identifier = getIdentifierByRaycast();
            if (identifier == null) throw NO_TARGET.create();
            Objects.requireNonNull(WikiPage.fromIdentifier(identifier)).openInBrowser(true);
            return 0;
        }));
    }
}