package wiki.minecraft.heywiki.entrypoint;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.mixin.GameRendererMixin;
import wiki.minecraft.heywiki.target.Target;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static wiki.minecraft.heywiki.HeyWikiClient.openWikiKey;
import static wiki.minecraft.heywiki.wiki.WikiPage.NO_FAMILY_MESSAGE;

/**
 * Raycasts from the crosshair to find a {@link Target} and opens the corresponding {@link WikiPage}.
 */
public class Raycast {
    private static final Minecraft CLIENT = Minecraft.getInstance();
    private static final HeyWikiClient MOD = HeyWikiClient.getInstance();

    /**
     * Should be called at {@link ClientTickEvent#CLIENT_POST ClientTickEvent#CLIENT_POST}.
     */
    public static void onClientTickPost(Minecraft client) {
        if (openWikiKey.consumeClick()) {
            Target target;
            if (Screen.hasAltDown()) {
                assert client.player != null;
                target = Target.of(client.player.getInventory().getSelectedItem());
            } else {
                target = Raycast.raycastWithMessage();
            }

            if (target != null) {
                var page = WikiPage.fromTarget(target);
                if (page == null) {
                    client.gui.setOverlayMessage(NO_FAMILY_MESSAGE, false);
                    return;
                }
                page.openInBrowser(null);
            }
        }
    }

    /**
     * Raycasts from the crosshair to find a {@link Target} and shows a message when the target is too far.
     *
     * @return The target found, or {@code null} if none was found.
     * @see GameRenderer#pick
     * @see #raycast()
     */
    public static @Nullable Target raycastWithMessage() {
        var target = raycast();
        if (target == null) {
            CLIENT.gui.setOverlayMessage(Component.translatable("gui.heywiki.too_far"), false);
        }
        return target;
    }

    /**
     * Raycasts from the crosshair to find a {@link Target}.
     *
     * @return The target found, or {@code null} if none was found.
     * @see GameRenderer#pick
     * @see #raycast()
     */
    public static @Nullable Target raycast() {
        assert CLIENT.player != null;
        assert CLIENT.level != null;

        double maxReach = MOD.config().raycastReach();
        double blockReach = Math.max(CLIENT.player.blockInteractionRange(), maxReach);
        double entityReach = Math.max(CLIENT.player.entityInteractionRange(), maxReach);
        HitResult hit = ((GameRendererMixin) CLIENT.gameRenderer).invokePick(
                CLIENT.cameraEntity,
                blockReach, entityReach,
                1f);

        switch (hit) {
            case EntityHitResult entityHit -> {
                var entity = entityHit.getEntity();
                return Target.of(entity);
            }
            case BlockHitResult blockHit -> {
                var blockPos = blockHit.getBlockPos();
                var blockState = CLIENT.level.getBlockState(blockPos);
                var block = blockState.getBlock();
                return Target.of(block);
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * Should be called at {@link ClientGuiEvent#DEBUG_TEXT_RIGHT ClientGuiEvent#DEBUG_TEXT_RIGHT}.
     */
    public static void onDebugTextRight(List<String> texts) {
        var target = Raycast.raycast();
        if (target == null) {
            texts.add("heywiki: null");
            return;
        }
        var page = WikiPage.fromTarget(target);
        if (page == null) {
            texts.add("heywiki: null");
            return;
        }
        texts.add("heywiki: " + URLDecoder.decode(page.getUri().toString(), StandardCharsets.UTF_8));
    }
}
