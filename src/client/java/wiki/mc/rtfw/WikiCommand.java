package wiki.mc.rtfw;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class WikiCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("wiki").then(argument("page", greedyString())
                .executes(ctx -> {
                    new WikiPage(getString(ctx, "page")).openInBrowser(MinecraftClient.getInstance().getLanguageManager().getLanguage(), true);
                    return 0;
                })));
    }
}