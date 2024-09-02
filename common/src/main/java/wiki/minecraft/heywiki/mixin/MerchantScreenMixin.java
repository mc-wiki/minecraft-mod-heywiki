package wiki.minecraft.heywiki.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wiki.minecraft.heywiki.extension.MerchantScreenInterface;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends HandledScreen<MerchantScreenHandler> implements
        MerchantScreenInterface {
    @Shadow
    @Final
    private MerchantScreen.WidgetButtonPage[] offers;

    @Shadow
    int indexStartOffset;

    public MerchantScreenMixin(MerchantScreenHandler handler,
                               PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Unique
    private int heywiki$mouseX;

    @Inject(method = "render", at = @At("HEAD"))
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo cir) {
        this.heywiki$mouseX = mouseX;
    }

    @Unique @Nullable public ItemStack heywiki$getHoveredStack() {
        for (MerchantScreen.WidgetButtonPage widgetButtonPage : this.offers) {
            if (widgetButtonPage.isHovered() &&
                this.handler.getRecipes().size() > widgetButtonPage.getIndex() + this.indexStartOffset) {
                if (heywiki$mouseX < widgetButtonPage.getX() + 20) {
                    return (this.handler.getRecipes().get(widgetButtonPage.getIndex() +
                                                          this.indexStartOffset))
                            .getDisplayedFirstBuyItem();
                } else if (heywiki$mouseX < widgetButtonPage.getX() + 50 &&
                           heywiki$mouseX > widgetButtonPage.getX() + 30) {
                    ItemStack itemStack = this.handler.getRecipes()
                                                      .get(widgetButtonPage.getIndex() + this.indexStartOffset)
                                                      .getDisplayedSecondBuyItem();
                    if (!itemStack.isEmpty()) {
                        return itemStack;
                    }
                } else if (heywiki$mouseX > widgetButtonPage.getX() + 65) {
                    return this.handler.getRecipes()
                                       .get(widgetButtonPage.getIndex() + this.indexStartOffset)
                                       .getSellItem();

                }
            }
        }
        return null;
    }
}
