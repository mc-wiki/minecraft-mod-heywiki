package wiki.minecraft.heywiki.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.recipebook.AnimatedResultButton;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.recipe.RecipeDisplayEntry;
import net.minecraft.recipe.display.SlotDisplayContexts;
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

@Mixin(RecipeBookWidget.class) public abstract class RecipeBookWidgetMixin {
    @Shadow
    @Final
    private RecipeBookResults recipesArea;

    @Shadow public abstract boolean isOpen();

    @Shadow
    private @Nullable TextFieldWidget searchField;

    @Shadow protected MinecraftClient client;

    @Inject(method = "keyPressed", at = @At("HEAD"))
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this.isOpen() && this.searchField != null && !this.searchField.isFocused() &&
            HeyWikiClient.openWikiKey.matchesKey(keyCode, scanCode)) {
            @Nullable AnimatedResultButton button = ((RecipeBookResultsMixin) recipesArea).heywiki$getHoveredResultButton();
            if (button != null) {
                RecipeDisplayEntry entry = button.getResultCollection().getAllRecipes().getFirst();
                assert this.client.world != null;
                var contextParameterMap = SlotDisplayContexts.createParameters(this.client.world);
                var target = Target.of(entry.display().result().getFirst(contextParameterMap));
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
}
