package wiki.minecraft.heywiki.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.command.suggestion.NSPageCombinedSuggestionProvider;
import wiki.minecraft.heywiki.wiki.WikiPage;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.argument;
import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.literal;

public class WikiCommand {
    private static final HeyWikiClient MOD = HeyWikiClient.getInstance();

    public static LiteralCommandNode<ClientCommandSourceStack> register(
            CommandDispatcher<ClientCommandSourceStack> dispatcher) {
        return dispatcher.register(
                literal("wiki")
                        .then(argument("page", greedyString())
                                      .suggests(new NSPageCombinedSuggestionProvider())
                                      .executes(ctx -> {
                                          String page = getString(ctx, "page");
                                          String[] pageSplit = page.split(":");
                                          if (pageSplit.length == 1) {
                                              new WikiPage(page,
                                                           MOD.familyManager().activeWikis().get("minecraft"))
                                                      .openInBrowserCommand(null);
                                              return 0;
                                          }

                                          var namespace = pageSplit[0];
                                          if (MOD.familyManager().getFamilyByNamespace(namespace) == null) {
                                              new WikiPage(page, MOD.familyManager().activeWikis().get("minecraft"))
                                                      .openInBrowserCommand(null);
                                              return 0;
                                          }

                                          new WikiPage(pageSplit[1],
                                                       MOD.familyManager().activeWikis().get(pageSplit[0]))
                                                  .openInBrowserCommand(null);
                                          return 0;
                                      })));
    }
}