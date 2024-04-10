package wiki.minecraft.heywiki.resource;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;
import wiki.minecraft.heywiki.wiki.WikiFamily;

import java.util.*;

public class WikiFamilyConfigManager extends JsonDataLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PATH = "wiki_family";
    private static final Gson GSON = new Gson();
    private static final Map<String, WikiFamily> WIKI_FAMILY_MAP = new HashMap<>();

    public WikiFamilyConfigManager() {
        super(GSON, PATH);
    }

    public static WikiFamily getFamily(String id) {
        return WIKI_FAMILY_MAP.get(id);
    }

    public static WikiFamily getFamilyByNamespace(String namespace) {
        for (WikiFamily family : WIKI_FAMILY_MAP.values()) {
            if (family.namespace.contains(namespace)) {
                return family;
            }
        }

        if (namespace.equals("minecraft")) {
            // This should never happen
            LOGGER.error("Failed to find family for namespace {}", namespace);
            return null;
        }

        return getFamilyByNamespace("minecraft");
    }

    @SuppressWarnings("unused")
    public static List<String> getAvailableLanguages(String id) {
        return getFamily(id).wikis.stream().map(wiki -> wiki.language.wikiLanguage).toList();
    }

    public static Set<String> getAvailableNamespaces() {
        return WIKI_FAMILY_MAP.values().stream().map(family -> family.namespace).collect(HashSet::new, Set::addAll, Set::addAll);
    }

    public static Set<String> getAllMainLanguages() {
        Set<String> languages = new HashSet<>();
        WIKI_FAMILY_MAP.forEach((key, value) -> value.wikis.forEach(wiki -> {
            if (wiki.language.main) languages.add(wiki.language.defaultLanguage);
        }));

        return languages;
    }

    public static Set<String> getAllDefaultLanguagesFromWikiLanguage(String wikiLanguage) {
        Set<String> languages = new HashSet<>();
        WIKI_FAMILY_MAP.forEach((key, value) -> value.wikis.forEach(wiki -> {
            if (wiki.language.wikiLanguage.equals(wikiLanguage)) languages.add(wiki.language.defaultLanguage);
        }));

        return languages;
    }

    public static Set<String> getLangOverride() {
        Set<String> languages = new HashSet<>();
        WIKI_FAMILY_MAP.forEach((key, value) -> {
            for (var wiki : value.wikis) {
                wiki.language.langOverride.ifPresent(languages::add);
            }

        });

        return languages;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        WIKI_FAMILY_MAP.clear();
        prepared.forEach((key, value) -> {
            try {
                WikiFamily wikiFamily = WikiFamily.CODEC.parse(JsonOps.INSTANCE, value).resultOrPartial(LOGGER::error).orElseThrow();
                WIKI_FAMILY_MAP.put(wikiFamily.id, wikiFamily);
            } catch (Exception e) {
                LOGGER.error("Failed to load wiki family config from {}", key, e);
            }
        });

        LOGGER.info("Loaded {} wiki families", WIKI_FAMILY_MAP.size());
    }
}
