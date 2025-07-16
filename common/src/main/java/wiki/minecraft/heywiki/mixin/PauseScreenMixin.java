package wiki.minecraft.heywiki.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import wiki.minecraft.heywiki.gui.screen.CallbackGameMenuScreen;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @WrapWithCondition(method = "method_19845", at = @At(value = "INVOKE",
                                                         target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    private boolean shouldSetScreenToNull(Minecraft instance, Screen screen) {
        if (screen != null) return true;

        if ((Screen) this instanceof CallbackGameMenuScreen that) {
            that.onClose();
        }

        return false;
    }
}
