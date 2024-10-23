package wiki.minecraft.heywiki.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wiki.minecraft.heywiki.extension.HandledScreenInterface;

@Mixin(StatusEffectsDisplay.class)
public class StatusEffectsDisplayMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(HandledScreen<?> parent, CallbackInfo ci) {
        ((HandledScreenInterface) parent).heywiki$setHasStatusEffect();
    }
}
