package wiki.minecraft.heywiki.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @WrapWithCondition(
            method = "keyPressed",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V")
    )
    private boolean shouldSetScreen(MinecraftClient instance, Screen screen) {
        // noinspection ConstantValue, RedundantCast
        if (screen == null && instance.currentScreen != (ChatScreen) (Object) this) return false;
        return true;
    }
}
