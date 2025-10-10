package wiki.minecraft.heywiki.mixin;

import com.google.common.collect.Ordering;
import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.extension.AbstractContainerScreenInterface;
import wiki.minecraft.heywiki.extension.MerchantScreenInterface;
import wiki.minecraft.heywiki.target.Target;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.util.Collection;

import static wiki.minecraft.heywiki.wiki.WikiPage.NO_FAMILY_MESSAGE;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends ScreenMixin implements AbstractContainerScreenInterface {
    @Shadow
    @Nullable
    protected Slot hoveredSlot;
    @Shadow
    protected int leftPos;
    @Shadow
    protected int topPos;
    @Shadow
    protected int imageWidth;

    @Unique
    private int heywiki$mouseX;
    @Unique
    private int heywiki$mouseY;
    @Unique
    private boolean heywiki$hasStatusEffect = false;

    @Inject(method = "render", at = @At("HEAD"))
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo cir) {
        this.heywiki$mouseX = mouseX;
        this.heywiki$mouseY = mouseY;
    }

    @Inject(method = "keyPressed", at = @At("HEAD"))
    public void keyPressed(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {
        if (!HeyWikiClient.openWikiKey.matches(keyEvent)) return;
        if (!heywiki$tryFocusedSlot()) {
            heywiki$tryStatusEffect();
        }
    }

    @Unique private boolean heywiki$tryFocusedSlot() {
        Slot slot = this.hoveredSlot;
        ItemStack stack = null;
        if (slot != null && slot.hasItem()) {
            stack = slot.getItem();
        } else if ((AbstractContainerScreen<?>) (Object) this instanceof MerchantScreen that) {
            stack = ((MerchantScreenInterface) that).heywiki$getHoveredStack();
        }

        if (stack == null) {
            return false;
        }

        var target = Target.of(stack);
        if (target != null) {
            var page = WikiPage.fromTarget(target);
            if (page == null) {
                Minecraft.getInstance().gui.setOverlayMessage(NO_FAMILY_MESSAGE, false);
                return false;
            }
            page.openInBrowser(Minecraft.getInstance().screen);
            return true;
        }

        return false;
    }

    @SuppressWarnings("UnusedReturnValue")
    @Unique private boolean heywiki$tryStatusEffect() {
        if (Platform.isModLoaded("emi") || !heywiki$hasStatusEffect) return false;

        var client = Minecraft.getInstance();
        assert client != null;

        int effectX = this.leftPos + this.imageWidth + 2;
        int availableWidth = this.width - effectX;
        assert client.player != null;
        Collection<MobEffectInstance> effectInstances = client.player.getActiveEffects();
        Iterable<MobEffectInstance> effectInstancesSorted = Ordering.natural().sortedCopy(effectInstances);
        int effectWidth = 33;
        if (availableWidth >= 120) {
            effectWidth = 121;
        }
        int effectHeight = 33;
        if (effectInstances.size() > 5) {
            effectHeight = 132 / (effectInstances.size() - 1);
        }

        if (availableWidth > 32 && this.heywiki$mouseX >= effectX && this.heywiki$mouseX <= effectX + effectWidth) {
            int effectY = this.topPos;
            MobEffectInstance effectFound = null;

            for (MobEffectInstance effect : effectInstancesSorted) {
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
                    client.gui.setOverlayMessage(NO_FAMILY_MESSAGE, false);
                    return false;
                }
                page.openInBrowser(Minecraft.getInstance().screen);
                return true;
            }
        }

        return false;
    }

    @Unique
    public void heywiki$setHasStatusEffect() {
        this.heywiki$hasStatusEffect = true;
    }
}