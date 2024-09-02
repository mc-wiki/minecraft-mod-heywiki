package wiki.minecraft.heywiki.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import wiki.minecraft.heywiki.gui.screen.CallbackGameMenuScreen;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @WrapWithCondition(method = "method_19845", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"))
    private boolean shouldSetScreenToNull(MinecraftClient instance, Screen screen) {
        if (screen != null) return true;

        if ((Screen) this instanceof CallbackGameMenuScreen that) {
            that.close();
        }

        return false;
    }
}
