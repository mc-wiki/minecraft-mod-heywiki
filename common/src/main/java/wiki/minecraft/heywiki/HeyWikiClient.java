package wiki.minecraft.heywiki;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.event.events.client.ClientChatEvent;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import wiki.minecraft.heywiki.command.*;
import wiki.minecraft.heywiki.entrypoint.ChatWikiLinks;
import wiki.minecraft.heywiki.entrypoint.Raycast;
import wiki.minecraft.heywiki.gui.screen.WikiSearchScreen;
import wiki.minecraft.heywiki.resource.WikiFamilyManager;
import wiki.minecraft.heywiki.resource.WikiTranslationManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.literal;

/**
 * The main class for the Hey Wiki mod.
 */
public class HeyWikiClient {
    public static final String MOD_ID = "heywiki";

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    private static final MinecraftClient client = MinecraftClient.getInstance();
    public static final KeyBinding openWikiKey = new KeyBinding("key.heywiki.open",
                                                                InputUtil.Type.KEYSYM,
                                                                GLFW.GLFW_KEY_H,
                                                                "key.categories.heywiki"
    );
    public static final KeyBinding openWikiSearchKey = new KeyBinding("key.heywiki.open_search",
                                                                      InputUtil.Type.KEYSYM,
                                                                      GLFW.GLFW_KEY_B,
                                                                      "key.categories.heywiki"
    );
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

        ClientGuiEvent.DEBUG_TEXT_RIGHT.register(Raycast::onDebugTextRight);

        ClientTickEvent.CLIENT_POST.register(Raycast::onClientTickPost);
        ClientTickEvent.CLIENT_POST.register(WikiSearchScreen::onClientTickPost);

        this.familyManager = new WikiFamilyManager();
        this.translationManager = new WikiTranslationManager();
        ReloadListenerRegistry.register(ResourceType.CLIENT_RESOURCES, this.familyManager, id("family"));
        ReloadListenerRegistry.register(ResourceType.CLIENT_RESOURCES, this.translationManager, id("translation"),
                                        List.of(id("family")));
    }

    private static void registerCommands(CommandDispatcher<ClientCommandSourceStack> dispatcher,
                                         CommandRegistryAccess registryAccess) {
        ImFeelingLuckyCommand.register(dispatcher);
        WhatBiomeCommand.register(dispatcher);
        var whatCommandCommand = WhatCommandCommand.register(dispatcher);
        WhatIsThisCommand.register(dispatcher);
        WhatIsThisItemCommand.register(dispatcher);
        var wikiCommand = WikiCommand.register(dispatcher);
        if (client.isIntegratedServerRunning()) {
            WhatStructureCommand.register(dispatcher);
        }

        dispatcher.register(literal("whatis").redirect(wikiCommand));
        dispatcher.register(literal("whatcmd").redirect(whatCommandCommand));
    }

    public static HeyWikiClient getInstance() {
        return INSTANCE;
    }

    private static final Set<String> experimentsWarned = new HashSet<>();
    private static final Set<String> deprecationsWarned = new HashSet<>();

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

    public WikiFamilyManager familyManager() {
        return familyManager;
    }

    public WikiTranslationManager translationManager() {
        return translationManager;
    }

    public HeyWikiConfig config() {
        return config;
    }
}