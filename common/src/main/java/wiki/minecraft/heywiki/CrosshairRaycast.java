package wiki.minecraft.heywiki;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import wiki.minecraft.heywiki.mixin.GameRendererMixin;
import wiki.minecraft.heywiki.wiki.Target;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.util.Objects;

import static wiki.minecraft.heywiki.HeyWikiClient.openWikiKey;

public class CrosshairRaycast {
    public static void onClientTickPost(MinecraftClient client) {
        while (openWikiKey.wasPressed()) {
            Target identifier = CrosshairRaycast.raycast(client, true);

            if (identifier != null) {
                Objects.requireNonNull(WikiPage.fromTarget(identifier)).openInBrowser();
            }
        }
    }

    public static @Nullable Target raycast() {
        return raycast(MinecraftClient.getInstance(), false);
    }

    public static @Nullable Target raycast(MinecraftClient client, boolean showTooFarMessage) {
        assert client.player != null;
        assert client.world != null;

        double maxReach = HeyWikiConfig.raycastReach;
        double blockReach = Math.max(client.player.getBlockInteractionRange(), maxReach);
        double entityReach = Math.max(client.player.getEntityInteractionRange(), maxReach);
        HitResult hit = ((GameRendererMixin) client.gameRenderer).invokeFindCrosshairTarget(client.cameraEntity, blockReach, entityReach, 1f);

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
