package wiki.minecraft.heywiki.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @WrapWithCondition(
            method = "keyPressed",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V")
    )
    private boolean shouldSetScreen(MinecraftClient instance, Screen screen) {
        return screen != null || instance.currentScreen == this;
    }
}
