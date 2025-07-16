package wiki.minecraft.heywiki.mixin;

import net.minecraft.client.resources.language.ClientLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(ClientLanguage.class)
public interface ClientLanguageFactory {
    @Invoker("<init>")
    static ClientLanguage create(Map<String, String> translations, boolean rightToLeft) {
        throw new AssertionError();
    }
}