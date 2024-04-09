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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import wiki.minecraft.heywiki.command.*;
import wiki.minecraft.heywiki.resource.PageNameSuggestionCacheManager;
import wiki.minecraft.heywiki.resource.WikiFamilyConfigManager;
import wiki.minecraft.heywiki.resource.WikiTranslationManager;
import wiki.minecraft.heywiki.wiki.IdentifierTranslationKey;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.util.List;
import java.util.Objects;

import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.literal;


public class HeyWikiClient {
    public static final String MOD_ID = "heywiki";
    public static KeyBinding openWikiKey = new KeyBinding("key.heywiki.open", // The translation key of the keybinding's name
            InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
            GLFW.GLFW_KEY_H, // The keycode of the key
            "key.categories.heywiki" // The translation key of the keybinding's category.
    );

    public static @Nullable IdentifierTranslationKey getIdentifierByRaycast() {
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
                    return new IdentifierTranslationKey(block.arch$registryName(), block.getTranslationKey());
                }
                break;
            case ENTITY:
                var entityHit = (EntityHitResult) hit;
                var entity = entityHit.getEntity();
                return new IdentifierTranslationKey(entity.getType().arch$registryName(), entity.getType().getTranslationKey());
        }

        client.inGameHud.setOverlayMessage(Text.translatable("heywiki.too_far"), false);
        return null;
    }

    private static void registerCommands(CommandDispatcher<ClientCommandSourceStack> dispatcher, CommandRegistryAccess registryAccess) {
        ImFeelingLuckyCommand.register(dispatcher);
        WhatBiomeCommand.register(dispatcher);
        var whatCommandCommand = WhatCommandCommand.register(dispatcher);
        WhatIsThisCommand.register(dispatcher);
        WhatIsThisItemCommand.register(dispatcher);
        var wikiCommand = WikiCommand.register(dispatcher);

        dispatcher.register(literal("whatis").redirect(wikiCommand));
        dispatcher.register(literal("whatcmd").redirect(whatCommandCommand));
    }

    public static void init() {
        HeyWikiConfig.load();

        KeyMappingRegistry.register(openWikiKey);

        ClientCommandRegistrationEvent.EVENT.register(HeyWikiClient::registerCommands);

        ClientTickEvent.CLIENT_POST.register(client -> {
            while (openWikiKey.wasPressed()) {
                IdentifierTranslationKey identifier = getIdentifierByRaycast();

                if (identifier != null) {
                    Objects.requireNonNull(WikiPage.fromIdentifier(identifier)).openInBrowser();
                }
            }
        });

        ReloadListenerRegistry.register(ResourceType.CLIENT_RESOURCES, new WikiFamilyConfigManager(), new Identifier("heywiki:family"));
        ReloadListenerRegistry.register(ResourceType.CLIENT_RESOURCES, new WikiTranslationManager(), new Identifier("heywiki:translation"), List.of(new Identifier("heywiki:family")));
        ReloadListenerRegistry.register(ResourceType.CLIENT_RESOURCES, new PageNameSuggestionCacheManager(), new Identifier("heywiki:page_name_suggestions"));
    }
}