package wiki.minecraft.heywiki.mixin;

import com.google.common.collect.Ordering;
import dev.architectury.platform.Platform;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.wiki.Target;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.util.Collection;
import java.util.Objects;

@Mixin(AbstractInventoryScreen.class)
public abstract class AbstractInventoryScreenMixin extends HandledScreenMixin {
    @Unique
    private int heywiki$mouseX;
    @Unique
    private int heywiki$mouseY;

    @Inject(method = "render", at = @At("HEAD"))
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo cir) {
        this.heywiki$mouseX = mouseX;
        this.heywiki$mouseY = mouseY;
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (HeyWikiClient.openWikiKey.matchesKey(keyCode, scanCode) && !Platform.isModLoaded("emi")) {
            var client = MinecraftClient.getInstance();
            assert client != null;

            int effectX = this.x + this.backgroundWidth + 2;
            int availableWidth = this.width - effectX;
            assert client.player != null;
            Collection<StatusEffectInstance> effectInstances = client.player.getStatusEffects();
            Iterable<StatusEffectInstance> effectInstancesSorted = Ordering.natural().sortedCopy(effectInstances);
            int effectWidth = 33;
            if (availableWidth >= 120) {
                effectWidth = 121;
            }
            int effectHeight = 33;
            if (effectInstances.size() > 5) {
                effectHeight = 132 / (effectInstances.size() - 1);
            }

            if (availableWidth > 32 && this.heywiki$mouseX >= effectX && this.heywiki$mouseX <= effectX + effectWidth) {
                int effectY = this.y;
                StatusEffectInstance effectFound = null;

                for (StatusEffectInstance effect : effectInstancesSorted) {
                    if (this.heywiki$mouseY >= effectY && this.heywiki$mouseY <= effectY + effectHeight) {
                        effectFound = effect;
                    }

                    effectY += effectHeight;
                }

                if (effectFound != null) {
                    var target = Target.of(effectFound);
                    assert target != null;
                    Objects.requireNonNull(WikiPage.fromTarget(target)).openInBrowser(false, MinecraftClient.getInstance().currentScreen);
                }
            }
            super.keyPressed(keyCode, scanCode, modifiers, cir);
        }
    }
}
