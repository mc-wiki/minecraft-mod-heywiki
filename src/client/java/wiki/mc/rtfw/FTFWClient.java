package wiki.mc.rtfw;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class FTFWClient implements ClientModInitializer {
    public static @Nullable URI buildUri(String pageName, String language) {
        String solvedLanguage = null;
        if (language.startsWith("de_")) {
            solvedLanguage = "de";
        } else if (language.startsWith("es_")) {
            solvedLanguage = "es";
        } else if (language.startsWith("fr_")) {
            solvedLanguage = "fr";
        } else if (language.startsWith("ja_")) {
            solvedLanguage = "ja";
        } else if (language.startsWith("lzh_")) {
            solvedLanguage = "lzh";
        } else if (language.startsWith("ko_")) {
            solvedLanguage = "ko";
        } else if (language.startsWith("pt_")) {
            solvedLanguage = "pt";
        } else if (language.startsWith("ru_")) {
            solvedLanguage = "ru";
        } else if (language.startsWith("th_")) {
            solvedLanguage = "th";
        } else if (language.startsWith("uk_")) {
            solvedLanguage = "uk";
        } else if (language.startsWith("zh_")) {
            solvedLanguage = "zh";
        }

        try {
            if (solvedLanguage == null)
                return new URI("https://minecraft.wiki/w/" + URLEncoder.encode(pageName.replaceAll(" ", "_"), StandardCharsets.UTF_8));
            else
                return new URI("https://" + solvedLanguage + ".minecraft.wiki/w/" + URLEncoder.encode(pageName.replaceAll(" ", "_"), StandardCharsets.UTF_8));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static @Nullable String getPageNameByRaycast() {
        var client = MinecraftClient.getInstance();
        var hit = client.crosshairTarget;

        switch (hit.getType()) {
            case MISS:
                break;
            case BLOCK:
                var blockHit = (BlockHitResult) hit;
                var blockPos = blockHit.getBlockPos();
                var blockState = client.world.getBlockState(blockPos);
                var block = blockState.getBlock();
                return block.getName().getString();
            case ENTITY:
                var entityHit = (EntityHitResult) hit;
                var entity = entityHit.getEntity();
                return entity.getName().getString();
        }
        return null;
    }

    @Override
    public void onInitializeClient() {
        var readKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.rtfw.open", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_SEMICOLON, // The keycode of the key
                "category.rtfw.rtfw" // The translation key of the keybinding's category.
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (readKey.wasPressed()) {
                var pageName = getPageNameByRaycast();
                if (pageName != null) {
                    var uri = buildUri(pageName, client.options.language);
                    Util.getOperatingSystem().open(uri);
                }
            }
        });
    }
}