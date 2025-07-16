package wiki.minecraft.heywiki.target;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.wiki.WikiIndividual;

import java.util.Objects;
import java.util.Optional;

/**
 * A target that can be created from an identifier and a translation key.
 *
 * @param identifier     The identifier of the target.
 * @param translationKey The translation key of the target.
 */
record IdentifierTarget(ResourceLocation identifier, Optional<String> translationKey, Optional<String> fallbackTitle)
        implements Target {
    public final static MapCodec<IdentifierTarget> CODEC = RecordCodecBuilder
            .mapCodec(builder ->
                              builder.group(
                                             ResourceLocation.CODEC.fieldOf("heywiki:identifier")
                                                                   .forGetter(target -> target.identifier),
                                             Codec.STRING.optionalFieldOf("heywiki:translation_key")
                                                         .forGetter(target -> target.translationKey),
                                             Codec.STRING.optionalFieldOf("heywiki:fallback_title")
                                                         .forGetter(target -> target.fallbackTitle))
                                     .apply(builder, IdentifierTarget::new));
    private static final Minecraft CLIENT = Minecraft.getInstance();
    private static final HeyWikiClient MOD = HeyWikiClient.getInstance();

    public IdentifierTarget(ResourceLocation identifier, String translationKey) {
        this(identifier, Optional.of(translationKey), Optional.empty());
    }

    @Override public String namespace() {
        return identifier.getNamespace();
    }

    @Override public String title() {
        var family = MOD.familyManager().getFamilyByNamespace(identifier.getNamespace());
        if (family == null) return null;

        WikiIndividual wiki = Objects.requireNonNull(MOD.familyManager().activeWikis().get(identifier.getNamespace()));

        ClientLanguage storage = MOD.translationManager().getTranslationOverride(wiki);
        String fallback = fallbackTitle().orElse(identifier.getPath());

        if (translationKey().isEmpty()) {
            return fallback;
        }

        var translationKey = translationKey().orElseThrow();

        if (storage != null && storage.has(translationKey)) {
            return storage.getOrDefault(translationKey, fallback);
        } else if (wiki.language().match(CLIENT.options.languageCode)) {
            return Language.getInstance().getOrDefault(translationKey, fallback);
        } else {
            return MOD.translationManager().getTranslations()
                      .get(wiki.language().defaultLanguage())
                      .getOrDefault(translationKey, fallback);
        }
    }
}
