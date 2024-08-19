package wiki.minecraft.heywiki.fabric.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import wiki.minecraft.heywiki.wiki.WikiPage;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    @Shadow @Final private static Logger LOGGER;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Unique
    private PressableTextWidget wikiButton;

    @Redirect(method = "render", at = @At(value = "INVOKE",
                                          target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)I"))
    private int drawTextWithShadow(DrawContext drawContext, TextRenderer textRenderer, String text, int x, int y,
                                   int color) {
        try {
            int width = textRenderer.getWidth(text);
            if (!this.children().contains(wikiButton)) {
                this.wikiButton = this.addDrawableChild(
                        new PressableTextWidget(x, y, width, 10, Text.literal(text),
                                                (button) -> {
                                                    var article = WikiPage.versionArticle(
                                                            SharedConstants.getGameVersion().getName());
                                                    if (article != null) {
                                                        article.openInBrowser(this);
                                                    }
                                                },
                                                this.textRenderer));
            }

            return 0;
        } catch (Exception e) {
            LOGGER.error("Failed to draw wiki button", e);
            drawContext.drawTextWithShadow(textRenderer, text, x, y, color);
            return 0;
        }
    }
}
