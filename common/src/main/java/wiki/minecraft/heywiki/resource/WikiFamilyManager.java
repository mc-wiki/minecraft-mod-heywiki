package wiki.minecraft.heywiki.resource;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.logging.LogUtils;
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
public class WikiFamilyManager extends JsonDataLoader<WikiFamily> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PATH = "wiki_family";
    private BiMap<Identifier, WikiFamily> WIKI_FAMILY_MAP = HashBiMap.create();
    private Map<String, WikiIndividual> activeWikis = new HashMap<>();

    public WikiFamilyManager() {
        super(WikiFamily.CODEC, PATH);
    }

    /**
     * Gets the family with the specified ID.
     *
     * @param id The ID.
     * @return The family.
     */
    public WikiFamily getFamily(Identifier id) {
        return WIKI_FAMILY_MAP.get(id);
    }

    /**
     * Gets the ID of the specified family.
     *
     * @param family The family.
     * @return The ID.
     */
    public Identifier getFamilyId(WikiFamily family) {
        return WIKI_FAMILY_MAP.inverse().get(family);
    }

    /**
     * Gets the family with the specified namespace.
     *
     * @param namespace The namespace.
     * @return The family.
     */
    public @Nullable WikiFamily getFamilyByNamespace(String namespace) {
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
    public Set<String> getAllAvailableLanguages() {
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
    public Set<String> getAvailableNamespaces() {
        return WIKI_FAMILY_MAP.values().stream().map(WikiFamily::namespace)
                              .collect(HashSet::new, Set::addAll, Set::addAll);
    }

    /**
     * Gets a set of all available wiki families.
     *
     * @return A set of wiki families.
     */
    public Set<WikiFamily> getAvailableFamilies() {
        return new HashSet<>(WIKI_FAMILY_MAP.values());
    }

    /**
     * Gets all default languages of every wiki in every family.
     *
     * @return A set of language codes.
     */
    public Set<String> getAllDefaultLanguages() {
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
    public Set<String> getAllDefaultLanguagesFromWikiLanguage(String wikiLanguage) {
        Set<String> languages = new HashSet<>();
        WIKI_FAMILY_MAP.forEach((key, value) -> value.wikis().forEach(wiki -> {
            if (wiki.language().wikiLanguage().equals(wikiLanguage)) languages.add(wiki.language().defaultLanguage());
        }));

        return languages;
    }

    /**
     * Gets all language overrides of every wiki in every family.
     *
     * @return A set of language codes for overrides.
     */
    public Set<String> getLangOverride() {
        Set<String> languages = new HashSet<>();
        WIKI_FAMILY_MAP.forEach((key, value) -> {
            for (var wiki : value.wikis()) {
                wiki.language().langOverride().ifPresent(languages::add);
            }

        });

        return languages;
    }

    /**
     * Get the current active {@link WikiIndividual} for each namespace.
     *
     * @return A map of namespaces to active wikis.
     * @see #resolveActiveWikis()
     */
    public Map<String, WikiIndividual> activeWikis() {
        return Collections.unmodifiableMap(this.activeWikis);
    }

    @Override
    protected void apply(Map<Identifier, WikiFamily> prepared, ResourceManager manager, Profiler profiler) {
        WIKI_FAMILY_MAP = HashBiMap.create(prepared);
        activeWikis = resolveActiveWikis();

        LOGGER.info("Loaded {} wiki families", WIKI_FAMILY_MAP.size());
    }

    /**
     * Resolves the active wikis for each namespace.
     *
     * @return A map of namespaces to active wikis.
     * @see #activeWikis
     */
    public Map<String, WikiIndividual> resolveActiveWikis() {
        Map<String, WikiIndividual> activeWikis = new HashMap<>();
        for (var entry : WIKI_FAMILY_MAP.entrySet()) {
            var family = entry.getValue();
            var wiki = family.getWiki();

            for (String namespace : family.namespace()) {
                activeWikis.put(namespace, wiki);
            }
        }

        return activeWikis;
    }
}
