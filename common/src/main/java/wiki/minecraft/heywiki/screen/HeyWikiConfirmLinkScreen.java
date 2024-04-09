package wiki.minecraft.heywiki.screen;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static wiki.minecraft.heywiki.HeyWikiClient.openWikiKey;

public class HeyWikiConfirmLinkScreen extends ConfirmLinkScreen {
    public HeyWikiConfirmLinkScreen(BooleanConsumer callback, String link) {
        this(callback, getConfirmText(true), Text.literal(URLDecoder.decode(link, StandardCharsets.UTF_8)), link, ScreenTexts.CANCEL);
    }

    private HeyWikiConfirmLinkScreen(BooleanConsumer callback, Text title, Text message, String link, Text noText) {
        super(callback, title, message, link, noText, true);
    }

    public static void open(Screen parent, String url) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        minecraftClient.setScreen(new HeyWikiConfirmLinkScreen((confirmed) -> {
            if (confirmed) {
                Util.getOperatingSystem().open(url);
            }

            minecraftClient.setScreen(parent);
        }, url));
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || openWikiKey.matchesKey(keyCode, scanCode)) {
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
