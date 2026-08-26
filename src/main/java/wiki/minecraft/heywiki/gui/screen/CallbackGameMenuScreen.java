package wiki.minecraft.heywiki.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.sounds.SoundSource;

import java.util.Objects;

public class CallbackGameMenuScreen extends PauseScreen {
    private final Runnable callback;

    public CallbackGameMenuScreen(boolean showMenu, Runnable callback) {
        super(showMenu);
        this.callback = callback;
    }

    public static void openWithParent(Screen parent, boolean showMenu) {
        var client = Minecraft.getInstance();
        boolean integratedServer =
                client.hasSingleplayerServer() && !Objects.requireNonNull(client.getSingleplayerServer())
                                                          .isPublished();
        if (integratedServer) {
            client.gui.setScreen(new CallbackGameMenuScreen(showMenu, () -> client.gui.setScreen(parent)));
            client.getSoundManager().pauseAllExcept(SoundSource.MUSIC, SoundSource.UI);
        } else {
            client.gui.setScreen(new CallbackGameMenuScreen(true, () -> client.gui.setScreen(parent)));
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        this.callback.run();
    }
}