package wiki.minecraft.heywiki.mixin;

import net.minecraft.client.resource.language.TranslationStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(TranslationStorage.class)
public interface TranslationStorageFactory {
    @Invoker("<init>")
    static TranslationStorage create(Map<String, String> translations, boolean rightToLeft) {
        throw new AssertionError();
    }
}