package wiki.minecraft.heywiki.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import wiki.minecraft.heywiki.MerchantScreenInterface;

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

    @Unique @Nullable public ItemStack heywiki$getHoveredStack() {
        assert this.client != null;
        Window window = this.client.getWindow();
        double scaleFactor = (double) window.getScaledWidth() / window.getWidth();
        double x = this.client.mouse.getX() * scaleFactor;

        for (MerchantScreen.WidgetButtonPage widgetButtonPage : this.offers) {
            if (widgetButtonPage.isHovered() &&
                this.handler.getRecipes().size() > widgetButtonPage.getIndex() + this.indexStartOffset) {
                if (x < widgetButtonPage.getX() + 20) {
                    return (this.handler.getRecipes().get(widgetButtonPage.getIndex() +
                                                          this.indexStartOffset))
                            .getDisplayedFirstBuyItem();
                } else if (x < widgetButtonPage.getX() + 50 && x > widgetButtonPage.getX() + 30) {
                    ItemStack itemStack = this.handler.getRecipes()
                                                      .get(widgetButtonPage.getIndex() + this.indexStartOffset)
                                                      .getDisplayedSecondBuyItem();
                    if (!itemStack.isEmpty()) {
                        return itemStack;
                    }
                } else if (x > widgetButtonPage.getX() + 65) {
                    return this.handler.getRecipes()
                                       .get(widgetButtonPage.getIndex() + this.indexStartOffset)
                                       .getSellItem();

                }
            }
        }
        return null;
    }
}
