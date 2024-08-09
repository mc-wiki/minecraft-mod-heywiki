package wiki.minecraft.heywiki;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import wiki.minecraft.heywiki.mixin.GameRendererMixin;
import wiki.minecraft.heywiki.wiki.Target;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.util.List;

import static wiki.minecraft.heywiki.HeyWikiClient.openWikiKey;
import static wiki.minecraft.heywiki.wiki.WikiPage.NO_FAMILY_MESSAGE;

/**
 * Raycasts from the crosshair to find a {@link Target} and opens the corresponding {@link WikiPage}.
 */
public class CrosshairRaycast {
    /**
     * Should be called at {@link dev.architectury.event.events.client.ClientTickEvent#CLIENT_POST ClientTickEvent#CLIENT_POST}.
     */
    public static void onClientTickPost(MinecraftClient client) {
        while (openWikiKey.wasPressed()) {
            Target target;
            if (Screen.hasAltDown()) {
                assert client.player != null;
                target = Target.of(client.player.getInventory().getMainHandStack());
            } else {
                target = CrosshairRaycast.raycast(client, true);
            }

            if (target != null) {
                var page = WikiPage.fromTarget(target);
                if (page == null) {
                    client.inGameHud.setOverlayMessage(NO_FAMILY_MESSAGE, false);
                    return;
                }
                page.openInBrowser();
            }
        }
    }

    /**
     * Should be called at {@link dev.architectury.event.events.client.ClientGuiEvent#DEBUG_TEXT_RIGHT ClientGuiEvent#DEBUG_TEXT_RIGHT}.
     */
    public static void onDebugTextRight(List<String> texts) {
        var target = CrosshairRaycast.raycast();
        if (target == null) {
            texts.add("heywiki: null");
            return;
        }
        var page = WikiPage.fromTarget(target);
        if (page == null) {
            texts.add("heywiki: null");
            return;
        }
        texts.add("heywiki: " + page.getUri());
    }

    /**
     * Raycasts from the crosshair to find a {@link Target}. It will not show a message when the target is too far.
     *
     * @return The target found, or {@code null} if none was found.
     * @see net.minecraft.client.render.GameRenderer#findCrosshairTarget
     * @see #raycast(MinecraftClient, boolean)
     */
    public static @Nullable Target raycast() {
        return raycast(MinecraftClient.getInstance(), false);
    }

    /**
     * Raycasts from the crosshair to find a {@link Target}.
     *
     * @param client            Should be {@link MinecraftClient#getInstance()}.
     * @param showTooFarMessage Whether to show a message when the target is too far.
     * @return The target found, or {@code null} if none was found.
     * @see net.minecraft.client.render.GameRenderer#findCrosshairTarget
     * @see #raycast()
     */
    public static @Nullable Target raycast(MinecraftClient client, boolean showTooFarMessage) {
        assert client.player != null;
        assert client.world != null;

        double maxReach = HeyWikiConfig.raycastReach;
        double blockReach = Math.max(client.player.getBlockInteractionRange(), maxReach);
        double entityReach = Math.max(client.player.getEntityInteractionRange(), maxReach);
        HitResult hit = ((GameRendererMixin) client.gameRenderer).invokeFindCrosshairTarget(
                client.cameraEntity,
                blockReach, entityReach,
                1f);

        switch (hit) {
            case EntityHitResult entityHit -> {
                var entity = entityHit.getEntity();
                return Target.of(entity);
            }
            case BlockHitResult blockHit -> {
                var blockPos = blockHit.getBlockPos();
                var blockState = client.world.getBlockState(blockPos);
                var block = blockState.getBlock();
                return Target.of(block);
            }
            default -> {
                if (showTooFarMessage) client.inGameHud.setOverlayMessage(Text.translatable("heywiki.too_far"), false);
            }
        }
        return null;
    }
}
