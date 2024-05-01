package wiki.minecraft.heywiki.fabric.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wiki.minecraft.heywiki.fabric.TitleScreenInterface;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    @Nullable
    public Screen currentScreen;

    @Inject(method = "setScreen", at = @At(value = "TAIL"))
    public void setScreen(CallbackInfo ci) {
        if (this.currentScreen instanceof TitleScreen titleScreen) {
            ((TitleScreenInterface) titleScreen).heywiki$resetInitialized();
        }
    }
}
