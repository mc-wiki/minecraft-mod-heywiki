package wiki.minecraft.heywiki;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.Nullable;
import wiki.minecraft.heywiki.wiki.IdentifierTranslationKey;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.util.Objects;

import static wiki.minecraft.heywiki.HeyWikiClient.openWikiKey;

public class CrosshairRaycast {
    public static void onClientTickPost(MinecraftClient client) {
        while (openWikiKey.wasPressed()) {
            IdentifierTranslationKey identifier = CrosshairRaycast.getIdentifierByRaycast(client, true);

            if (identifier != null) {
                Objects.requireNonNull(WikiPage.fromIdentifier(identifier)).openInBrowser();
            }
        }
    }

    public static @Nullable IdentifierTranslationKey getIdentifierByRaycast() {
        return getIdentifierByRaycast(MinecraftClient.getInstance(), false);
    }

    public static @Nullable IdentifierTranslationKey getIdentifierByRaycast(MinecraftClient client, boolean showTooFarMessage) {
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
            if (entity instanceof ItemEntity itemEntity) {
                ItemStack stack = itemEntity.getStack();
                return new IdentifierTranslationKey(stack.getItem().arch$registryName(), stack.getTranslationKey());
            }
            return new IdentifierTranslationKey(entity.getType().arch$registryName(), entity.getType().getTranslationKey());
        }
        if (blockHit != null) {
            var blockPos = blockHit.getBlockPos();
            var blockState = client.world.getBlockState(blockPos);
            var block = blockState.getBlock();
            if (!Objects.requireNonNull(block.arch$registryName()).toString().equals("minecraft:air"))
                return new IdentifierTranslationKey(block.arch$registryName(), block.getTranslationKey());
        }

        if (showTooFarMessage) client.inGameHud.setOverlayMessage(Text.translatable("heywiki.too_far"), false);
        return null;
    }
}
