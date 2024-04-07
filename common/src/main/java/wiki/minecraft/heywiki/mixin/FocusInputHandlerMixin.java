package wiki.minecraft.heywiki.mixin;

import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.FocusInputHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(FocusInputHandler.class)
public class FocusInputHandlerMixin {
    @Inject(method = "handleUserInput", at = @At("HEAD"), remap = false)
    private void handleUserInput(net.minecraft.client.gui.screens.Screen screen, UserInput input, IInternalKeyMappings keyBindings, CallbackInfoReturnable<Optional<IUserInputHandler>> cir) {
        if (input.is(input.getKey())) {

        }
    }
}
