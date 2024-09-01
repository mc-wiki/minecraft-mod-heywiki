package wiki.minecraft.heywiki.mixin;

import net.minecraft.client.gui.screen.recipebook.AnimatedResultButton;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeBookResults.class)
public interface RecipeBookResultsMixin {
    @Accessor("hoveredResultButton")
    @Nullable AnimatedResultButton heywiki$getHoveredResultButton();
}
