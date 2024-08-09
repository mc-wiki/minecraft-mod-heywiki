package wiki.minecraft.heywiki.resource;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import wiki.minecraft.heywiki.wiki.WikiFamily;
import wiki.minecraft.heywiki.wiki.WikiIndividual;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages the configuration of wiki families.
 *
 * @see WikiFamily
 */
public class WikiFamilyConfigManager extends JsonDataLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PATH = "wiki_family";
    private static final Gson GSON = new Gson();
    private static final Map<String, WikiFamily> WIKI_FAMILY_MAP = new HashMap<>();
    /**
     * The current active {@link WikiIndividual} for each namespace.
     *
     * @see #resolveActiveWikis()
     */
    public static Map<String, WikiIndividual> activeWikis = new HashMap<>();

    public WikiFamilyConfigManager() {
        super(GSON, PATH);
    }

    /**
     * Gets the family with the specified ID.
     *
     * @param id The ID.
     * @return The family.
     */
    public static WikiFamily getFamily(String id) {
        return WIKI_FAMILY_MAP.get(id);
    }

    /**
     * Gets the family with the specified namespace.
     *
     * @param namespace The namespace.
     * @return The family.
     */
    public static @Nullable WikiFamily getFamilyByNamespace(String namespace) {
        for (WikiFamily family : WIKI_FAMILY_MAP.values()) {
            if (family.namespace().contains(namespace)) {
                return family;
            }
        }

        return null;
    }

    /**
     * Gets all available languages in every family.
     *
     * @return A set of language codes.
     */
    public static Set<String> getAllAvailableLanguages() {
        return WIKI_FAMILY_MAP.values().stream()
                              .flatMap(family -> family.wikis().stream().map(wiki -> wiki.language().wikiLanguage()))
                              .sorted(Comparator.naturalOrder())
                              .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Gets all available namespaces in every family.
     *
     * @return A set of namespaces.
     */
    public static Set<String> getAvailableNamespaces() {
        return WIKI_FAMILY_MAP.values().stream().map(WikiFamily::namespace)
                              .collect(HashSet::new, Set::addAll, Set::addAll);
    }

    /**
     * Gets all default languages of every individual wiki in every family.
     *
     * @return A set of language codes.
     */
    public static Set<String> getAllDefaultLanguages() {
        Set<String> languages = new HashSet<>();
        WIKI_FAMILY_MAP.forEach((key, value) -> value.wikis().forEach(wiki -> {
            if (wiki.language().main()) languages.add(wiki.language().defaultLanguage());
        }));

        return languages;
    }

    /**
     * Gets all default languages in every family that match the specified wiki language.
     *
     * @param wikiLanguage The wiki language.
     * @return A set of language codes.
     */
    public static Set<String> getAllDefaultLanguagesFromWikiLanguage(String wikiLanguage) {
        Set<String> languages = new HashSet<>();
        WIKI_FAMILY_MAP.forEach((key, value) -> value.wikis().forEach(wiki -> {
            if (wiki.language().wikiLanguage().equals(wikiLanguage)) languages.add(wiki.language().defaultLanguage());
        }));

        return languages;
    }

    /**
     * Gets all language overrides of every individual wiki in every family.
     *
     * @return A set of language codes for overrides.
     */
    public static Set<String> getLangOverride() {
        Set<String> languages = new HashSet<>();
        WIKI_FAMILY_MAP.forEach((key, value) -> {
            for (var wiki : value.wikis()) {
                wiki.language().langOverride().ifPresent(languages::add);
            }

        });

        return languages;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        WIKI_FAMILY_MAP.clear();
        prepared.forEach((key, value) -> {
            try {
                WikiFamily wikiFamily = WikiFamily.CODEC.parse(JsonOps.INSTANCE, value).resultOrPartial(LOGGER::error)
                                                        .orElseThrow();
                WIKI_FAMILY_MAP.put(wikiFamily.id(), wikiFamily);
            } catch (Exception e) {
                LOGGER.error("Failed to load wiki family config from {}", key, e);
            }
        });
        activeWikis = resolveActiveWikis();

        LOGGER.info("Loaded {} wiki families", WIKI_FAMILY_MAP.size());
    }

    /**
     * Resolves the active wikis for each namespace.
     *
     * @return A map of namespaces to active wikis.
     * @see #activeWikis
     */
    public static Map<String, WikiIndividual> resolveActiveWikis() {
        Map<String, WikiIndividual> activeWikis = new HashMap<>();
        for (var entry : WikiFamilyConfigManager.WIKI_FAMILY_MAP.entrySet()) {
            var family = entry.getValue();
            var wiki = family.getWiki();

            for (String namespace : family.namespace()) {
                activeWikis.put(namespace, wiki);
            }
        }

        return activeWikis;
    }
}
