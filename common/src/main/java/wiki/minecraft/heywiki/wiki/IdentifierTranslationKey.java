package wiki.minecraft.heywiki.wiki;

import net.minecraft.util.Identifier;

public class IdentifierTranslationKey {
    Identifier identifier;
    String translationKey;

    public IdentifierTranslationKey(Identifier identifier, String translationKey) {
        this.identifier = identifier;
        this.translationKey = translationKey;
    }
}
