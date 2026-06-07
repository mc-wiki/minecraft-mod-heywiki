package wiki.minecraft.heywiki.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static wiki.minecraft.heywiki.HeyWikiClient.id;

@Mixin(SplashManager.class)
public class SplashManagerMixin {
    @Unique
    private static final Identifier heywiki$resourceId = id("texts/splashes.txt");
    @Unique
    private static final Logger heywiki$logger = LogUtils.getLogger();

    @Invoker("literalSplash")
    private static Component literalSplash(String string) {
        throw new AssertionError();
    }

    @ModifyReturnValue(
            method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Ljava/util/List;",
            at = @At("RETURN")
    )
    private List<Component> heywikiSplashes(List<Component> originalSplashes) {
        try {
            List<Component> splashes = new ArrayList<>(originalSplashes);
            try (BufferedReader splashesReader = Minecraft.getInstance().getResourceManager()
                                                          .openAsReader(heywiki$resourceId)) {
                splashes.addAll(splashesReader.lines().map(String::trim).map(SplashManagerMixin::literalSplash).toList());
            }

            return splashes;
        } catch (IOException e) {
            heywiki$logger.warn("Failed to load splashes from {}", heywiki$resourceId, e);
        }
        return originalSplashes;
    }
}
