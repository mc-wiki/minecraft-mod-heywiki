package wiki.minecraft.heywiki.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.extension.MerchantScreenInterface;
import wiki.minecraft.heywiki.wiki.WikiPage;
import wiki.minecraft.heywiki.target.Target;

import static wiki.minecraft.heywiki.wiki.WikiPage.NO_FAMILY_MESSAGE;

@Mixin(HandledScreen.class)
public class HandledScreenMixin extends ScreenMixin {
    @Shadow
    @Nullable
    protected Slot focusedSlot;
    @Shadow
    protected int x;
    @Shadow
    protected int y;
    @Shadow
    protected int backgroundWidth;

    @Inject(method = "keyPressed", at = @At("HEAD"))
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (HeyWikiClient.openWikiKey.matchesKey(keyCode, scanCode)) {
            Slot slot = this.focusedSlot;
            ItemStack stack = null;
            if (slot != null && slot.hasStack()) {
                stack = slot.getStack();
            } else if ((HandledScreen<?>) (Object) this instanceof MerchantScreen that) {
                stack = ((MerchantScreenInterface) that).heywiki$getHoveredStack();
            }

            if (stack == null) {
                MinecraftClient.getInstance().inGameHud.setOverlayMessage(NO_FAMILY_MESSAGE, false);
                return;
            }

            var target = Target.of(stack);
            if (target != null) {
                var page = WikiPage.fromTarget(target);
                if (page == null) {
                    MinecraftClient.getInstance().inGameHud.setOverlayMessage(NO_FAMILY_MESSAGE, false);
                    return;
                }
                page.openInBrowser(MinecraftClient.getInstance().currentScreen);
            }
        }
    }
}