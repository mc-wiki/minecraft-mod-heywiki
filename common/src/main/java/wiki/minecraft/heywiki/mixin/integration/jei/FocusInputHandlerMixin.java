package wiki.minecraft.heywiki.mixin.integration.jei;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.CombinedRecipeFocusSource;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.FocusInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.target.Target;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.util.Optional;

import static wiki.minecraft.heywiki.wiki.WikiPage.NO_FAMILY_MESSAGE;

@Pseudo
@Mixin(FocusInputHandler.class)
public class FocusInputHandlerMixin {
    @Shadow(remap = false)
    @Final
    private CombinedRecipeFocusSource focusSource;

    @Inject(method = "handleUserInput", at = @At("HEAD"), remap = false)
    private void handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input,
                                 IInternalKeyMappings keyBindings,
                                 CallbackInfoReturnable<Optional<IUserInputHandler>> cir) {
        if (input.is(HeyWikiClient.openWikiKey)) {
            focusSource.getIngredientUnderMouse(input, keyBindings)
                       .filter(clicked -> clicked.getElement().isVisible())
                       .findFirst()
                       .ifPresent(clicked -> {
                           var itemStack = clicked.getTypedIngredient().getItemStack();
                           if (itemStack.isPresent()) {
                               var target = Target.of(itemStack.get());
                               if (target != null) {
                                   var page = WikiPage.fromTarget(target);
                                   if (page == null) {
                                       Minecraft.getInstance().gui.setOverlayMessage(NO_FAMILY_MESSAGE,
                                                                                     false);
                                       return;
                                   }
                                   page.openInBrowser(null);
                               }
                           }
                       });
        }
    }
}