package wiki.minecraft.heywiki.gametest.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wiki.minecraft.heywiki.gametest.GameTestClientEntry;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onCreate(CallbackInfo ci) {
        GameTestClientEntry.onClientStarted(MinecraftClient.getInstance());
    }
}
