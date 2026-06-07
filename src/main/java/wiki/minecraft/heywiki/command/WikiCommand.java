package wiki.minecraft.heywiki.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.SharedSuggestionProvider;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.command.suggestion.NSPageCombinedSuggestionProvider;
import wiki.minecraft.heywiki.wiki.WikiPage;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static wiki.minecraft.heywiki.platform.HeyWikiPlatform.literal;
import static wiki.minecraft.heywiki.platform.HeyWikiPlatform.argument;

public class WikiCommand {
    private static final HeyWikiClient MOD = HeyWikiClient.getInstance();

    public static LiteralCommandNode<SharedSuggestionProvider> register(
            CommandDispatcher<SharedSuggestionProvider> dispatcher) {
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
                                              return Command.SINGLE_SUCCESS;
                                          }

                                          var namespace = pageSplit[0];
                                          if (MOD.familyManager().getFamilyByNamespace(namespace) == null) {
                                              new WikiPage(page, MOD.familyManager().activeWikis().get("minecraft"))
                                                      .openInBrowserCommand(null);
                                              return Command.SINGLE_SUCCESS;
                                          }

                                          new WikiPage(pageSplit[1],
                                                       MOD.familyManager().activeWikis().get(pageSplit[0]))
                                                  .openInBrowserCommand(null);
                                          return Command.SINGLE_SUCCESS;
                                      })));
    }
}