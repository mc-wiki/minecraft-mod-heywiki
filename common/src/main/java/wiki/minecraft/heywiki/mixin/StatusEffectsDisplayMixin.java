package wiki.minecraft.heywiki.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wiki.minecraft.heywiki.extension.AbstractContainerScreenInterface;

@Mixin(EffectsInInventory.class)
public class StatusEffectsDisplayMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(AbstractContainerScreen<?> parent, CallbackInfo ci) {
        ((AbstractContainerScreenInterface) parent).heywiki$setHasStatusEffect();
    }
}
