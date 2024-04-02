package wiki.minecraft.heywiki;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import wiki.minecraft.heywiki.command.*;

import java.util.concurrent.CompletableFuture;

import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.literal;


public class HeyWikiClient {
    public static final String MOD_ID = "heywiki";
    public static KeyBinding readKey = new KeyBinding("key.heywiki.open", // The translation key of the keybinding's name
            InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
            GLFW.GLFW_KEY_H, // The keycode of the key
            "category.heywiki.heywiki" // The translation key of the keybinding's category.
    );

    public static @Nullable String getTranslationKeyByRaycast() {
        var client = MinecraftClient.getInstance();
        var hit = client.crosshairTarget;

        if (hit == null) return null;

        switch (hit.getType()) {
            case MISS:
                break;
            case BLOCK:
                var blockHit = (BlockHitResult) hit;
                var blockPos = blockHit.getBlockPos();
                if (client.world != null) {
                    var blockState = client.world.getBlockState(blockPos);
                    var block = blockState.getBlock();
                    return block.getTranslationKey();
                }
                break;
            case ENTITY:
                var entityHit = (EntityHitResult) hit;
                var entity = entityHit.getEntity();
                return entity.getType().getTranslationKey();
        }

        return null;
    }

    private static void registerCommands(CommandDispatcher<ClientCommandSourceStack> dispatcher, CommandRegistryAccess registryAccess) {
        WhatBiomeCommand.register(dispatcher);
        var whatCommandCommand = WhatCommandCommand.register(dispatcher);
        WhatIsThisCommand.register(dispatcher);
        WhatIsThisItemCommand.register(dispatcher);
        var wikiCommand = WikiCommand.register(dispatcher);

        dispatcher.register(literal("whatis").redirect(wikiCommand));
        dispatcher.register(literal("whatcmd").redirect(whatCommandCommand));
    }

    public static void init() {
        HeyWikiConfig.HANDLER.load();

        KeyMappingRegistry.register(readKey);

        ClientCommandRegistrationEvent.EVENT.register(HeyWikiClient::registerCommands);

        ClientTickEvent.CLIENT_POST.register(client -> {
            while (readKey.wasPressed()) {
                String translationKey = getTranslationKeyByRaycast();

                if (translationKey != null) {
                    WikiPage.fromTranslationKey(translationKey).openInBrowser();
                }
            }
        });

        ReloadListenerRegistry.register(ResourceType.CLIENT_RESOURCES,
                (synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor) ->
                        CompletableFuture
                                .completedFuture(null)
                                .thenCompose(synchronizer::whenPrepared)
                                .thenCompose(t -> CompletableFuture.runAsync(() -> WikiPage.fromTranslationKey(""), applyExecutor)));
    }
}