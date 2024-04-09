package wiki.minecraft.heywiki.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.util.Objects;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {
    @Shadow
    @Nullable
    protected Slot focusedSlot;

    @Inject(method = "keyPressed", at = @At("HEAD"))
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (HeyWikiClient.openWikiKey.matchesKey(keyCode, scanCode)) {
            Slot slot = this.focusedSlot;
            if (slot != null && slot.hasStack()) {
                Item item = slot.getStack().getItem();
                Identifier registryName = item.arch$registryName();
                if (registryName == null) return;
                Objects.requireNonNull(WikiPage.fromIdentifier(registryName, item.getTranslationKey())).openInBrowser(false, MinecraftClient.getInstance().currentScreen);
            }
        }
    }
}