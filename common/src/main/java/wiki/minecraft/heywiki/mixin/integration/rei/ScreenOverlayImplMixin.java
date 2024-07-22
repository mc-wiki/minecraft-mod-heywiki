package wiki.minecraft.heywiki.mixin.integration.rei;

import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.wiki.Target;
import wiki.minecraft.heywiki.wiki.WikiPage;

import static wiki.minecraft.heywiki.wiki.WikiPage.NO_FAMILY_MESSAGE;

@Pseudo
@Mixin(EntryWidget.class)
public abstract class ScreenOverlayImplMixin {
    // This is so hacky
    @Inject(method = "keyPressedIgnoreContains", at = @At("HEAD"), remap = false)
    public void keyPressedIgnoreContains(int keyCode, int scanCode, int modifiers,
                                         CallbackInfoReturnable<Boolean> cir) {
        if (REIRuntime.getInstance().isOverlayVisible()) {
            EntryStack<?> stack = this.getCurrentEntry();
            if (stack != null && !stack.isEmpty()) {
                stack = stack.copy();
                if (HeyWikiClient.openWikiKey.matchesKey(keyCode, scanCode)) {
                    if (stack.getValue() instanceof ItemStack itemStack) {
                        var target = Target.of(itemStack);
                        if (target != null) {
                            var page = WikiPage.fromTarget(target);
                            if (page == null) {
                                MinecraftClient.getInstance().inGameHud.setOverlayMessage(NO_FAMILY_MESSAGE, false);
                                return;
                            }
                            page.openInBrowser();
                        }
                    }
                }
            }
        }
    }

    @Shadow(remap = false)
    public abstract EntryStack<?> getCurrentEntry();
}
