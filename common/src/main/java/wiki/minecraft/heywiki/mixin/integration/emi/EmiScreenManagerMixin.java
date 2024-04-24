package wiki.minecraft.heywiki.mixin.integration.emi;

import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.screen.EmiScreenManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.wiki.Target;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.util.Objects;

import static dev.emi.emi.api.EmiApi.getHoveredStack;

@Mixin(EmiScreenManager.class)
public class EmiScreenManagerMixin {
    @Inject(method = "keyPressed", at = @At("HEAD"), remap = false)
    @SuppressWarnings("UnstableApiUsage")
    private static void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        EmiStackInteraction stackInteraction = getHoveredStack(false);
        if (!stackInteraction.isEmpty() && stackInteraction instanceof EmiScreenManager.SidebarEmiStackInteraction
                && stackInteraction.getStack() instanceof ItemEmiStack itemEmiStack) {
            if (HeyWikiClient.openWikiKey.matchesKey(keyCode, scanCode)) {
                var target = Target.of(itemEmiStack.getItemStack());
                if (target != null)
                    Objects.requireNonNull(WikiPage.fromTarget(target)).openInBrowser();
            }
        }
    }
}
