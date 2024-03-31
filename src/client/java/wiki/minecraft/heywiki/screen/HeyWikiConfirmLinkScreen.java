package wiki.minecraft.heywiki.screen;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

public class HeyWikiConfirmLinkScreen extends ConfirmLinkScreen {
    public HeyWikiConfirmLinkScreen(BooleanConsumer callback, String link, boolean linkTrusted) {
        super(callback, link, linkTrusted);
    }

    public static void open(Screen parent, String url) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        minecraftClient.setScreen(new HeyWikiConfirmLinkScreen((confirmed) -> {
            if (confirmed) {
                Util.getOperatingSystem().open(url);
            }

            minecraftClient.setScreen(parent);
        }, url, true));
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            this.callback.accept(true);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_C && hasControlDown() && !hasShiftDown() && !hasAltDown()) {
            this.callback.accept(false);
            this.copyToClipboard();
            return false;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
