package wiki.minecraft.heywiki.mixin.integration.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.screen.RecipeScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.wiki.Target;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.util.Objects;

@Mixin(RecipeScreen.class)
public abstract class RecipeScreenMixin {
    @Shadow(remap = false)
    public abstract EmiIngredient getHoveredStack();

    @Inject(method = "keyPressed", at = @At("HEAD"))
    @SuppressWarnings("UnstableApiUsage")
    private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        EmiIngredient ingredient = this.getHoveredStack();
        if (!ingredient.isEmpty() && ingredient.getEmiStacks().get(0) instanceof ItemEmiStack itemEmiStack) {
            if (HeyWikiClient.openWikiKey.matchesKey(keyCode, scanCode)) {
                if (HeyWikiClient.openWikiKey.matchesKey(keyCode, scanCode)) {
                    var target = Target.of((itemEmiStack.getItemStack()));
                    if (target != null)
                        Objects.requireNonNull(WikiPage.fromTarget(target)).openInBrowser();
                }
            }
        }
    }
}
