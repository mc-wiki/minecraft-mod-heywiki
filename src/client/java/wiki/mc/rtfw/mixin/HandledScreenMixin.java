package wiki.mc.rtfw.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wiki.mc.rtfw.FTFWClient;

import java.net.URI;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {
    @Shadow
    @Nullable
    protected Slot focusedSlot;

    @Inject(method = "keyPressed", at = @At("HEAD"))
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (FTFWClient.readKey.matchesKey(keyCode, scanCode)) {
            Slot slot = this.focusedSlot;
            if (slot != null && slot.hasStack()) {
                String pageName = slot.getStack().getItem().getName().getString();
                String language = MinecraftClient.getInstance().getLanguageManager().getLanguage();
                URI uri = FTFWClient.buildUri(pageName, language);
                if (uri != null) {
                    Util.getOperatingSystem().open(uri);
                }
            }
        }
    }
}