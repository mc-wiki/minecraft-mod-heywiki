package wiki.minecraft.heywiki.mixin.integration.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.screen.RecipeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.target.Target;
import wiki.minecraft.heywiki.wiki.WikiPage;

import static wiki.minecraft.heywiki.wiki.WikiPage.NO_FAMILY_MESSAGE;

@Pseudo
@Mixin(RecipeScreen.class)
public abstract class RecipeScreenMixin {
    @Inject(method = "keyPressed", at = @At("HEAD"))
    @SuppressWarnings("UnstableApiUsage")
    private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        EmiIngredient ingredient = this.getHoveredStack();
        var keyEvent = new KeyEvent(keyCode, scanCode, modifiers);
        if (!ingredient.isEmpty() && ingredient.getEmiStacks().getFirst() instanceof ItemEmiStack itemEmiStack) {
            if (HeyWikiClient.openWikiKey.matches(keyEvent)) {
                if (HeyWikiClient.openWikiKey.matches(keyEvent)) {
                    var target = Target.of((itemEmiStack.getItemStack()));
                    if (target != null) {
                        var page = WikiPage.fromTarget(target);
                        if (page == null) {
                            Minecraft.getInstance().gui.setOverlayMessage(NO_FAMILY_MESSAGE, false);
                            return;
                        }
                        page.openInBrowser(null);
                    }
                }
            }
        }
    }

    @Shadow(remap = false)
    public abstract EmiIngredient getHoveredStack();
}
