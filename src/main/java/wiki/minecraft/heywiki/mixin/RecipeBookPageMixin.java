package wiki.minecraft.heywiki.mixin;

import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeBookPage.class)
public interface RecipeBookPageMixin {
    @Accessor("hoveredButton")
    @Nullable RecipeButton heywiki$getHoveredResultButton();
}
