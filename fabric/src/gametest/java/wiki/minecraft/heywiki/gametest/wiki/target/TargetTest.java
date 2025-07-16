package wiki.minecraft.heywiki.gametest.wiki.target;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import org.lwjgl.glfw.GLFW;
import wiki.minecraft.heywiki.gui.screen.ConfirmWikiPageScreen;

@SuppressWarnings("UnstableApiUsage")
public class TargetTest implements FabricClientGameTest {
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            context.runOnClient((client) -> {
                assert client.player != null;
                client.player.setYRot(0);
            });
            context.getInput().holdKey(GLFW.GLFW_KEY_LEFT_ALT);
            context.getInput().holdKey(GLFW.GLFW_KEY_H);
            context.waitTicks(10);
            context.getInput().releaseKey(GLFW.GLFW_KEY_H);
            context.getInput().releaseKey(GLFW.GLFW_KEY_LEFT_ALT);
            context.waitTick();
            context.waitForScreen(ConfirmWikiPageScreen.class);
            context.runOnClient((client) -> {
                assert !(client.screen instanceof ConfirmWikiPageScreen screen) ||
                       screen.getTitle().getString().equals("Grass Block");
            });
        }
    }
}