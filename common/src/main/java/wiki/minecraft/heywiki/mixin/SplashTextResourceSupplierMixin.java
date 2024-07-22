package wiki.minecraft.heywiki.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(method = "prepare*", at = @At("RETURN"), cancellable = true)
    private void prepare(CallbackInfoReturnable<List<String>> cir) {
        try {
            List<String> splashes = new ArrayList<>(cir.getReturnValue());
            try (BufferedReader splashesReader = MinecraftClient.getInstance().getResourceManager()
                                                                .openAsReader(heywiki$resourceId)) {
                splashes.addAll(splashesReader.lines().map(String::trim).toList());
            }

            cir.setReturnValue(splashes);
        } catch (IOException e) {
            heywiki$logger.warn("Failed to load splashes from {}", heywiki$resourceId, e);
        }
    }
}
