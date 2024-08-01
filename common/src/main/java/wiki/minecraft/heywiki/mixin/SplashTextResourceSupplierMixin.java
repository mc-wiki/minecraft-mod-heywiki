package wiki.minecraft.heywiki.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Mixin(SplashTextResourceSupplier.class)
public class SplashTextResourceSupplierMixin {
    @Unique
    private static final Identifier heywiki$resourceId = Identifier.of("heywiki", "texts/splashes.txt");
    @Unique
    private static final Logger heywiki$logger = LogUtils.getLogger();

    @ModifyReturnValue(
            method = "prepare*",
            at = @At("RETURN")
    )
    private List<String> heywikiSplashes(List<String> originalSplashes) {
        try {
            List<String> splashes = new ArrayList<>(originalSplashes);
            try (BufferedReader splashesReader = MinecraftClient.getInstance().getResourceManager()
                                                                .openAsReader(heywiki$resourceId)) {
                splashes.addAll(splashesReader.lines().map(String::trim).toList());
            }

            return splashes;
        } catch (IOException e) {
            heywiki$logger.warn("Failed to load splashes from {}", heywiki$resourceId, e);
        }
        return originalSplashes;
    }
}
