package wiki.minecraft.heywiki.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.recipebook.AnimatedResultButton;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.recipe.RecipeEntry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.wiki.WikiPage;
import wiki.minecraft.heywiki.wiki.target.Target;

import static wiki.minecraft.heywiki.wiki.WikiPage.NO_FAMILY_MESSAGE;

@Mixin(RecipeBookWidget.class)
public abstract class RecipeBookWidgetMixin {
    @Shadow
    @Final
    private RecipeBookResults recipesArea;

    @Shadow public abstract boolean isOpen();

    @Shadow
    private @Nullable TextFieldWidget searchField;

    @Inject(method = "keyPressed", at = @At("HEAD"))
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this.isOpen() &&
            this.searchField != null && !this.searchField.isFocused() &&
            HeyWikiClient.openWikiKey.matchesKey(keyCode, scanCode)) {
            if (HeyWikiClient.openWikiKey.matchesKey(keyCode, scanCode)) {
                @Nullable AnimatedResultButton button = ((RecipeBookResultsMixin) recipesArea).heywiki$getHoveredResultButton();
                if (button != null) {
                    RecipeEntry<?> entry = button.getResultCollection().getResults(false).getFirst();
                    assert MinecraftClient.getInstance().world != null;
                    var target = Target.of(
                            entry.value().getResult(MinecraftClient.getInstance().world.getRegistryManager()));
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
}
