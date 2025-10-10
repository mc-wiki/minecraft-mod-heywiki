package wiki.minecraft.heywiki.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
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
public abstract class MerchantScreenMixin extends AbstractContainerScreen<MerchantMenu> implements
        MerchantScreenInterface {
    @Shadow
    int scrollOff;
    @Shadow
    @Final
    private MerchantScreen.TradeOfferButton[] tradeOfferButtons;
    @Unique
    private int heywiki$mouseX;

    public MerchantScreenMixin(MerchantMenu menu,
                               Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "renderContents", at = @At("HEAD"))
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo cir) {
        this.heywiki$mouseX = mouseX;
    }

    @Unique @Nullable public ItemStack heywiki$getHoveredStack() {
        for (MerchantScreen.TradeOfferButton widgetButtonPage : this.tradeOfferButtons) {
            if (widgetButtonPage.isHovered() &&
                this.menu.getOffers().size() > widgetButtonPage.getIndex() + this.scrollOff) {
                if (heywiki$mouseX < widgetButtonPage.getX() + 20) {
                    return (this.menu.getOffers().get(widgetButtonPage.getIndex() +
                                                          this.scrollOff))
                            .getCostA();
                } else if (heywiki$mouseX < widgetButtonPage.getX() + 50 &&
                           heywiki$mouseX > widgetButtonPage.getX() + 30) {
                    ItemStack itemStack = this.menu.getOffers()
                                                      .get(widgetButtonPage.getIndex() + this.scrollOff)
                                                      .getCostB();
                    if (!itemStack.isEmpty()) {
                        return itemStack;
                    }
                } else if (heywiki$mouseX > widgetButtonPage.getX() + 65) {
                    return this.menu.getOffers()
                                       .get(widgetButtonPage.getIndex() + this.scrollOff)
                                       .getResult();

                }
            }
        }
        return null;
    }
}
