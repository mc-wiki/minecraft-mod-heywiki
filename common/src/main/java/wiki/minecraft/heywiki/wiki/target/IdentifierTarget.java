package wiki.minecraft.heywiki.wiki.target;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.wiki.WikiIndividual;

import java.util.Optional;

/**
 * A target that can be created from an identifier and a translation key.
 *
 * @param identifier     The identifier of the target.
 * @param translationKey The translation key of the target.
 */
public record IdentifierTarget(Identifier identifier, Optional<String> translationKey, Optional<String> fallbackTitle)
        implements Target {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final HeyWikiClient MOD = HeyWikiClient.getInstance();
    public final static MapCodec<IdentifierTarget> CODEC = RecordCodecBuilder
            .mapCodec(builder ->
                              builder.group(
                                             Identifier.CODEC.fieldOf("heywiki:identifier")
                                                             .forGetter(target -> target.identifier),
                                             Codec.STRING.optionalFieldOf("heywiki:translation_key")
                                                         .forGetter(target -> target.translationKey),
                                             Codec.STRING.optionalFieldOf("heywiki:fallback_title")
                                                         .forGetter(target -> target.fallbackTitle))

                                     .apply(builder, IdentifierTarget::new));

    public IdentifierTarget(Identifier identifier, String translationKey) {
        this(identifier, Optional.of(translationKey), Optional.empty());
    }

    @Override public String namespace() {
        return identifier.getNamespace();
    }

    @Override public String title() {
        var family = MOD.familyManager().getFamilyByNamespace(identifier.getNamespace());
        if (family == null) return null;

        WikiIndividual wiki = MOD.familyManager().activeWikis().get(identifier.getNamespace());

        TranslationStorage storage = MOD.translationManager().getTranslationOverride(wiki);
        String fallback = fallbackTitle().orElse(identifier.getPath());

        if (translationKey().isEmpty()) {
            return fallback;
        }

        var translationKey = translationKey().orElseThrow();

        if (storage != null && storage.hasTranslation(translationKey)) {
            return storage.get(translationKey, fallback);
        } else if (wiki.language().match(CLIENT.options.language)) {
            return Language.getInstance().get(translationKey, fallback);
        } else {
            return MOD.translationManager().getTranslations()
                      .get(wiki.language().defaultLanguage())
                      .get(translationKey, fallback);
        }
    }
}
