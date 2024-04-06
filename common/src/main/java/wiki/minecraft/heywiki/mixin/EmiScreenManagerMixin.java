package wiki.minecraft.heywiki.mixin;

import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.util.Objects;

import static dev.emi.emi.api.EmiApi.getHoveredStack;

@Mixin(EmiScreenManager.class)
public class EmiScreenManagerMixin {
    @Inject(method = "keyPressed", at = @At("HEAD"), remap = false)
    @SuppressWarnings("UnstableApiUsage")
    private static void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        EmiStackInteraction stackInteraction = getHoveredStack(false);
        if (!stackInteraction.isEmpty() && stackInteraction instanceof EmiScreenManager.SidebarEmiStackInteraction && stackInteraction.getStack() instanceof ItemEmiStack) {
            if (HeyWikiClient.openWikiKey.matchesKey(keyCode, scanCode)) {
                ItemStack itemStack = ((ItemEmiStack) stackInteraction.getStack()).getItemStack();
                Identifier registryName = itemStack.getItem().arch$registryName();
                if (registryName != null) {
                    Objects.requireNonNull(WikiPage.fromIdentifier(registryName, itemStack.getTranslationKey())).openInBrowser();
                }
            }
        }
    }
}
