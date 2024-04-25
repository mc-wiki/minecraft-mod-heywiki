package wiki.minecraft.heywiki;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.Nullable;
import wiki.minecraft.heywiki.wiki.Target;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.util.List;
import java.util.Objects;

public class CrosshairRaycast {
    public static void onClientTickPost(MinecraftClient client) {
        Target target;
        if (Screen.hasAltDown()) {
            assert client.player != null;
            target = Target.of(client.player.getInventory().getMainHandStack());
        } else {
            target = CrosshairRaycast.raycast(client, true);
        }

        if (target != null) {
            Objects.requireNonNull(WikiPage.fromTarget(target)).openInBrowser();
        }
    }

    public static void onDebugTextRight(List<String> texts) {

        var target = CrosshairRaycast.raycast();
        if (target == null) {
            texts.add("heywiki: null");
            return;
        }
        var page = Objects.requireNonNull(WikiPage.fromTarget(target));
        texts.add("heywiki: " + page.getUri());
    }

    public static @Nullable Target raycast() {
        return raycast(MinecraftClient.getInstance(), false);
    }

    public static @Nullable Target raycast(MinecraftClient client, boolean showTooFarMessage) {
        float tickDelta = 1.0F;
        double maxReach = HeyWikiConfig.raycastMaxReach;
        Entity camera = client.cameraEntity;

        if (client.cameraEntity == null || client.world == null) return null;

        Vec3d rotationVec = camera.getRotationVec(tickDelta);
        Vec3d startVec = camera.getCameraPosVec(tickDelta);
        Vec3d endVec = startVec.add(rotationVec.multiply(maxReach));
        Box box = camera.getBoundingBox().stretch(rotationVec.multiply(maxReach)).expand(1.0, 1.0, 1.0);
        EntityHitResult entityHit = ProjectileUtil.raycast(camera, startVec, endVec, box, (entity) -> (!entity.isSpectator() && entity.canHit()) || entity instanceof ItemEntity, maxReach * maxReach);

        BlockHitResult blockHit = client.world.raycast(new RaycastContext(startVec, endVec, RaycastContext.ShapeType.OUTLINE,
                HeyWikiConfig.raycastAllowFluid ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, camera));

        boolean shouldUseBlock = entityHit != null && blockHit != null &&
                entityHit.getPos().distanceTo(startVec) > blockHit.getPos().distanceTo(startVec);

        if (entityHit != null && !shouldUseBlock) {
            var entity = entityHit.getEntity();
            return Target.of(entity);
        }
        if (blockHit != null) {
            var blockPos = blockHit.getBlockPos();
            var blockState = client.world.getBlockState(blockPos);
            var block = blockState.getBlock();
            return Target.of(block);
        }

        if (showTooFarMessage) client.inGameHud.setOverlayMessage(Text.translatable("heywiki.too_far"), false);
        return null;
    }
}
