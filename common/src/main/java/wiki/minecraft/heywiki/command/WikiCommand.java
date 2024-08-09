package wiki.minecraft.heywiki.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import wiki.minecraft.heywiki.command.suggestion.NSPageCombinedSuggestionProvider;
import wiki.minecraft.heywiki.wiki.WikiPage;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.argument;
import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.literal;
import static wiki.minecraft.heywiki.resource.WikiFamilyConfigManager.activeWikis;

public class WikiCommand {
    public static LiteralCommandNode<ClientCommandSourceStack> register(
            CommandDispatcher<ClientCommandSourceStack> dispatcher) {
        return dispatcher.register(
                literal("wiki")
                        .then(argument("page", greedyString())
                                      .suggests(new NSPageCombinedSuggestionProvider())
                                      .executes(ctx -> {
                                          String page = getString(ctx, "page");
                                          String[] pageSplitted = page.split(":");
                                          if (pageSplitted.length == 1) {
                                              new WikiPage(page, activeWikis.get(
                                                      "minecraft")).openInBrowser(true);
                                              return 0;
                                          }
                                          new WikiPage(pageSplitted[1], activeWikis.get(
                                                  pageSplitted[0])).openInBrowser(true);
                                          return 0;
                                      })));
    }
}