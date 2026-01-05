package wiki.minecraft.heywiki;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.event.events.client.*;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import wiki.minecraft.heywiki.command.*;
import wiki.minecraft.heywiki.entrypoint.ChatWikiLinks;
import wiki.minecraft.heywiki.entrypoint.HeyWikiDebugEntry;
import wiki.minecraft.heywiki.entrypoint.Raycast;
import wiki.minecraft.heywiki.gui.screen.WikiSearchScreen;
import wiki.minecraft.heywiki.mixin.DebugScreenEntriesMixin;
import wiki.minecraft.heywiki.resource.WikiFamilyManager;
import wiki.minecraft.heywiki.resource.WikiTranslationManager;
import wiki.minecraft.heywiki.target.Target;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.literal;

/**
 * The main class for the Hey Wiki mod.
 */
public class HeyWikiClient {
    public static final String MOD_ID = "heywiki";
    public static final KeyMapping.Category HEYWIKI_CATEGORY = new KeyMapping.Category(id("heywiki"));
    public static final KeyMapping openWikiKey = new KeyMapping("key.heywiki.open",
                                                                InputConstants.Type.KEYSYM,
                                                                GLFW.GLFW_KEY_H,
                                                                HEYWIKI_CATEGORY
    );
    public static final KeyMapping openWikiSearchKey = new KeyMapping("key.heywiki.open_search",
                                                                      InputConstants.Type.KEYSYM,
                                                                      GLFW.GLFW_KEY_B,
                                                                      HEYWIKI_CATEGORY
    );
    private static final Set<String> experimentsWarned = new HashSet<>();
    private static final Set<String> deprecationsWarned = new HashSet<>();
    private static HeyWikiClient INSTANCE;
    private final WikiFamilyManager familyManager;
    private final WikiTranslationManager translationManager;
    private final HeyWikiConfig config;

    /**
     * Initializes the Hey Wiki mod. Should be called at client setup.
     */
    public HeyWikiClient() {
        INSTANCE = this;

        this.config = HeyWikiConfig.load();

        KeyMappingRegistry.register(openWikiKey);
        KeyMappingRegistry.register(openWikiSearchKey);

        ClientCommandRegistrationEvent.EVENT.register(HeyWikiClient::registerCommands);

        ClientChatEvent.RECEIVED.register(ChatWikiLinks::onClientChatReceived);

        DebugScreenEntriesMixin.invokeRegister(HeyWikiClient.id("url"), new HeyWikiDebugEntry());

        ClientTooltipEvent.ITEM.register((stack, lines, tooltipContex, flag) -> {
            if (!config().itemTooltip()) return;
            if (openWikiKey.isUnbound()) return;

            var target = Target.of(stack);
            if (target == null) return;

            var family = familyManager().getFamilyByNamespace(target.namespace());
            if (family == null) return;

            lines.add(Component.translatable("gui.heywiki.tooltip",
                                             openWikiKey.getTranslatedKeyMessage().copy()
                                                        .setStyle(Style.EMPTY.withColor(
                                                                ChatFormatting.GRAY))
                                            ).setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)));
        });

        ClientTickEvent.CLIENT_POST.register(Raycast::onClientTickPost);
        ClientTickEvent.CLIENT_POST.register(WikiSearchScreen::onClientTickPost);

        this.familyManager = new WikiFamilyManager();
        this.translationManager = new WikiTranslationManager();
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, this.familyManager, id("family"));
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, this.translationManager, id("translation"),
                                        List.of(id("family")));
    }

    private static void registerCommands(CommandDispatcher<ClientCommandSourceStack> dispatcher,
                                         CommandBuildContext buildContext) {
        ImFeelingLuckyCommand.register(dispatcher);
        WhatBiomeCommand.register(dispatcher);
        var whatCommandCommand = WhatCommandCommand.register(dispatcher);
        WhatIsThisCommand.register(dispatcher);
        WhatIsThisItemCommand.register(dispatcher);
        var wikiCommand = WikiCommand.register(dispatcher);
        var whatVersionCommand = WhatVersionCommand.register(dispatcher);
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            WhatStructureCommand.register(dispatcher);
        }

        dispatcher.register(literal("whatis").redirect(wikiCommand));
        dispatcher.register(literal("whatcmd").redirect(whatCommandCommand));
        dispatcher.register(literal("whatver").redirect(whatVersionCommand));
    }

    public HeyWikiConfig config() {
        return config;
    }

    public WikiFamilyManager familyManager() {
        return familyManager;
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static HeyWikiClient getInstance() {
        return INSTANCE;
    }

    /**
     * Logs a warning that a feature is experimental.
     *
     * @param feature The name of the experimental feature.
     */
    public static void experimentalWarning(Logger logger, String feature) {
        if (experimentsWarned.add(feature)) {
            logger.warn(
                    "{} is an experimental feature. It is subject to breaking changes in future minor or patch releases.",
                    feature);
        }
    }

    /**
     * Logs a warning that a feature is deprecated.
     *
     * @param feature The name of the deprecated feature.
     */
    public static void deprecatedWarning(Logger logger, String feature) {
        if (deprecationsWarned.add(feature)) {
            logger.warn(
                    "{} is an experimental feature. It is subject to breaking changes in future minor or patch releases.",
                    feature);
        }
    }

    public WikiTranslationManager translationManager() {
        return translationManager;
    }
}