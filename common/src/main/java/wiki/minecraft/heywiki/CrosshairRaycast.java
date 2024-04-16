package wiki.minecraft.heywiki;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import wiki.minecraft.heywiki.mixin.GameRendererMixin;
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
        assert client.player != null;
        assert client.world != null;

        double maxReach = HeyWikiConfig.raycastReach;
        double blockReach = Math.max(client.player.getBlockInteractionRange(), maxReach);
        double entityReach = Math.max(client.player.getEntityInteractionRange(), maxReach);
        HitResult hit = ((GameRendererMixin) client.gameRenderer).invokeFindCrosshairTarget(client.cameraEntity, blockReach, entityReach, 1f);

        if (hit instanceof EntityHitResult entityHit) {
            var entity = entityHit.getEntity();
            if (entity instanceof ItemEntity itemEntity) {
                ItemStack stack = itemEntity.getStack();
                return new IdentifierTranslationKey(stack.getItem().arch$registryName(), stack.getTranslationKey());
            }
            return new IdentifierTranslationKey(entity.getType().arch$registryName(), entity.getType().getTranslationKey());
        } else if (hit instanceof BlockHitResult blockHit) {
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
