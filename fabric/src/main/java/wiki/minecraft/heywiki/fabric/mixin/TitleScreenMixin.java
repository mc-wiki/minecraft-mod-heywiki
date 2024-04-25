package wiki.minecraft.heywiki.fabric.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import wiki.minecraft.heywiki.wiki.WikiPage;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)I"))
    private int drawTextWithShadow(DrawContext drawContext, TextRenderer textRenderer, String text, int x, int y, int color) {
        var article = WikiPage.versionArticle(SharedConstants.getGameVersion().getName());
        if (article == null) return drawContext.drawTextWithShadow(textRenderer, text, x, y, color);

        int width = textRenderer.getWidth(text);
        this.addDrawableChild(new PressableTextWidget(x, y, width, 10, Text.literal(text),
                (button) -> article.openInBrowser(false, this), this.textRenderer));

        return 0;
    }
}
