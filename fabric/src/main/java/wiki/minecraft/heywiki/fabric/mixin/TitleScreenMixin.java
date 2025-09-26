package wiki.minecraft.heywiki.fabric.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
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
    @Shadow
    @Final
    private static Logger LOGGER;
    @Unique
    private PlainTextButton wikiButton;

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
                                          target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"))
    private void drawTextWithShadow(GuiGraphics instance, Font font, String text, int x, int y, int color) {
        try {
            int width = font.width(text);
            if (!this.children().contains(wikiButton)) {
                this.wikiButton = this.addRenderableWidget(
                        new PlainTextButton(x, y, width, 10, Component.literal(text),
                                            (button) -> {
                                                var article = WikiPage.versionArticle(
                                                        SharedConstants.getCurrentVersion().name());
                                                if (article != null) {
                                                    article.openInBrowser(this);
                                                }
                                            },
                                            this.font));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to draw wiki button", e);
            instance.drawString(font, text, x, y, color);
        }
    }
}
