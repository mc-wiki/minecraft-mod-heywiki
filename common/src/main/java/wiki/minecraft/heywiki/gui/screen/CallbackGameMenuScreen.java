package wiki.minecraft.heywiki.gui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.sound.SoundCategory;

import java.util.Objects;

public class CallbackGameMenuScreen extends GameMenuScreen {
    private final Runnable callback;

    public CallbackGameMenuScreen(boolean showMenu, Runnable callback) {
        super(showMenu);
        this.callback = callback;
    }

    @Override
    public void close() {
        super.close();
        this.callback.run();
    }

    public static void openWithParent(Screen parent, boolean showMenu) {
        var client = MinecraftClient.getInstance();
        boolean integratedServer = client.isIntegratedServerRunning() && !Objects.requireNonNull(client.getServer())
                                                                                 .isRemote();
        if (integratedServer) {
            client.setScreen(new CallbackGameMenuScreen(showMenu, () -> client.setScreen(parent)));
            client.getSoundManager().pauseAllExcept(SoundCategory.MUSIC, SoundCategory.UI);
        } else {
            client.setScreen(new CallbackGameMenuScreen(true, () -> client.setScreen(parent)));
        }
    }
}