package wiki.mc.rtfw;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;


public class FTFWClient implements ClientModInitializer {
    public static KeyBinding readKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.rtfw.open", // The translation key of the keybinding's name
            InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
            GLFW.GLFW_KEY_SEMICOLON, // The keycode of the key
            "category.rtfw.rtfw" // The translation key of the keybinding's category.
    ));

    private static @Nullable String getPageNameByRaycast() {
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
                    return block.getName().getString();
                }
                break;
            case ENTITY:
                var entityHit = (EntityHitResult) hit;
                var entity = entityHit.getEntity();
                return entity.getName().getString();
        }

        return null;
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        WikiCommand.register(dispatcher);
    }

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(FTFWClient::registerCommands);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (readKey.wasPressed()) {
                String pageName = getPageNameByRaycast();

                if (pageName != null) {
                    new WikiPage(pageName).openInBrowser(client.options.language);
                }
            }
        });
    }

}