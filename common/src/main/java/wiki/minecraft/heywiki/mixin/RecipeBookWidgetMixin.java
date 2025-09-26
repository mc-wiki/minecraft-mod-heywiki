package wiki.minecraft.heywiki.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.target.Target;
import wiki.minecraft.heywiki.wiki.WikiPage;

import static wiki.minecraft.heywiki.wiki.WikiPage.NO_FAMILY_MESSAGE;

@Mixin(RecipeBookComponent.class) public abstract class RecipeBookWidgetMixin {
    @Shadow
    protected Minecraft minecraft;
    @Shadow
    @Final
    private RecipeBookPage recipeBookPage;
    @Shadow
    private @Nullable EditBox searchBox;

    @Inject(method = "keyPressed", at = @At("HEAD"))
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this.isVisible() && this.searchBox != null && !this.searchBox.isFocused() &&
            HeyWikiClient.openWikiKey.matches(keyCode, scanCode)) {
            @Nullable RecipeButton button = ((RecipeBookPageMixin) recipeBookPage).heywiki$getHoveredResultButton();
            if (button != null) {
                RecipeDisplayEntry entry = button.getCollection().getRecipes().getFirst();
                assert this.minecraft.level != null;
                var contextParameterMap = SlotDisplayContext.fromLevel(this.minecraft.level);
                var target = Target.of(entry.display().result().resolveForFirstStack(contextParameterMap));
                if (target != null) {
                    var page = WikiPage.fromTarget(target);
                    if (page == null) {
                        Minecraft.getInstance().gui.setOverlayMessage(NO_FAMILY_MESSAGE, false);
                        return;
                    }
                    page.openInBrowser(Minecraft.getInstance().screen);
                }
            }
        }
    }

    @Shadow public abstract boolean isVisible();
}
