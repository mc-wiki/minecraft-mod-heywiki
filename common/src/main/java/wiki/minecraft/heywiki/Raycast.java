package wiki.minecraft.heywiki;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import wiki.minecraft.heywiki.wiki.IdentifierTranslationKey;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.util.Objects;

import static wiki.minecraft.heywiki.HeyWikiClient.openWikiKey;

public class Raycast {
    public static void onClientTickPost(MinecraftClient client) {
        while (openWikiKey.wasPressed()) {
            IdentifierTranslationKey identifier = Raycast.getIdentifierByRaycast(client, true);

            if (identifier != null) {
                Objects.requireNonNull(WikiPage.fromIdentifier(identifier)).openInBrowser();
            }
        }
    }

    public static @Nullable IdentifierTranslationKey getIdentifierByRaycast() {
        return getIdentifierByRaycast(MinecraftClient.getInstance(), false);
    }

    public static @Nullable IdentifierTranslationKey getIdentifierByRaycast(MinecraftClient client, boolean showTooFarMessage) {
        HitResult hit = client.crosshairTarget;

        if (hit == null) return null;

        switch (hit.getType()) {
            case MISS:
                break;
            case BLOCK:
                var blockHit = (BlockHitResult) hit;
                var blockPos = blockHit.getBlockPos();
                if (client.world != null) {
                    var blockState = client.world.getBlockState(blockPos);
                    var block = blockState.getBlock();
                    return new IdentifierTranslationKey(block.arch$registryName(), block.getTranslationKey());
                }
                break;
            case ENTITY:
                var entityHit = (EntityHitResult) hit;
                var entity = entityHit.getEntity();
                return new IdentifierTranslationKey(entity.getType().arch$registryName(), entity.getType().getTranslationKey());
        }

        if (showTooFarMessage) client.inGameHud.setOverlayMessage(Text.translatable("heywiki.too_far"), false);
        return null;
    }
}
