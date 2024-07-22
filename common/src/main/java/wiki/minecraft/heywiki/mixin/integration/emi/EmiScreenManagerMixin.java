package wiki.minecraft.heywiki.mixin.integration.emi;

import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.wiki.Target;
import wiki.minecraft.heywiki.wiki.WikiPage;

import static dev.emi.emi.api.EmiApi.getHoveredStack;
import static wiki.minecraft.heywiki.wiki.WikiPage.NO_FAMILY_MESSAGE;

@Pseudo
@Mixin(EmiScreenManager.class)
public class EmiScreenManagerMixin {
    @Shadow
    private static MinecraftClient client;

    @Inject(method = "keyPressed", at = @At("HEAD"), remap = false)
    @SuppressWarnings("UnstableApiUsage")
    private static void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        EmiStackInteraction stackInteraction = getHoveredStack(false);
        if (!stackInteraction.isEmpty() && stackInteraction instanceof EmiScreenManager.SidebarEmiStackInteraction
            && stackInteraction.getStack() instanceof ItemEmiStack itemEmiStack) {
            if (HeyWikiClient.openWikiKey.matchesKey(keyCode, scanCode)) {
                var target = Target.of(itemEmiStack.getItemStack());
                if (target != null) {
                    var page = WikiPage.fromTarget(target);
                    if (page == null) {
                        client.inGameHud.setOverlayMessage(NO_FAMILY_MESSAGE, false);
                        return;
                    }
                    page.openInBrowser();
                }
            }
        }
    }
}
