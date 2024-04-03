package wiki.minecraft.heywiki.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import wiki.minecraft.heywiki.WikiPage;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.argument;
import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.literal;

public class WikiCommand {
    public static LiteralCommandNode<ClientCommandSourceStack> register(CommandDispatcher<ClientCommandSourceStack> dispatcher) {
        return dispatcher.register(literal("wiki").then(argument("page", greedyString())
                .executes(ctx -> {
                    new WikiPage(getString(ctx, "page")).openInBrowser(true);
                    return 0;
                })));
    }
}