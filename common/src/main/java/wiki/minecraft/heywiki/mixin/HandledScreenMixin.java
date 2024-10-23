package wiki.minecraft.heywiki.mixin;

import com.google.common.collect.Ordering;
import dev.architectury.platform.Platform;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.extension.HandledScreenInterface;
import wiki.minecraft.heywiki.extension.MerchantScreenInterface;
import wiki.minecraft.heywiki.target.Target;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.util.Collection;

import static wiki.minecraft.heywiki.wiki.WikiPage.NO_FAMILY_MESSAGE;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends ScreenMixin implements HandledScreenInterface {
    @Shadow
    @Nullable
    protected Slot focusedSlot;
    @Shadow
    protected int x;
    @Shadow
    protected int y;
    @Shadow
    protected int backgroundWidth;

    @Unique
    private int heywiki$mouseX;
    @Unique
    private int heywiki$mouseY;

    @Inject(method = "render", at = @At("HEAD"))
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo cir) {
        this.heywiki$mouseX = mouseX;
        this.heywiki$mouseY = mouseY;
    }

    @Inject(method = "keyPressed", at = @At("HEAD"))
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!HeyWikiClient.openWikiKey.matchesKey(keyCode, scanCode)) return;
        if (!heywiki$tryFocusedSlot()) {
            heywiki$tryStatusEffect();
        }
    }

    @Unique
    private boolean heywiki$hasStatusEffect = false;

    @Unique
    public void heywiki$setHasStatusEffect() {
        this.heywiki$hasStatusEffect = true;
    }

    @SuppressWarnings("UnusedReturnValue")
    @Unique private boolean heywiki$tryStatusEffect() {
        if (Platform.isModLoaded("emi") || !heywiki$hasStatusEffect) return false;

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
                var page = WikiPage.fromTarget(target);
                if (page == null) {
                    client.inGameHud.setOverlayMessage(NO_FAMILY_MESSAGE, false);
                    return false;
                }
                page.openInBrowser(MinecraftClient.getInstance().currentScreen);
                return true;
            }
        }

        return false;
    }

    @Unique private boolean heywiki$tryFocusedSlot() {
        Slot slot = this.focusedSlot;
        ItemStack stack = null;
        if (slot != null && slot.hasStack()) {
            stack = slot.getStack();
        } else if ((HandledScreen<?>) (Object) this instanceof MerchantScreen that) {
            stack = ((MerchantScreenInterface) that).heywiki$getHoveredStack();
        }

        if (stack == null) {
            return false;
        }

        var target = Target.of(stack);
        if (target != null) {
            var page = WikiPage.fromTarget(target);
            if (page == null) {
                MinecraftClient.getInstance().inGameHud.setOverlayMessage(NO_FAMILY_MESSAGE, false);
                return false;
            }
            page.openInBrowser(MinecraftClient.getInstance().currentScreen);
            return true;
        }

        return false;
    }
}