package wiki.minecraft.heywiki.resource;

import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;

import java.util.HashMap;

public class PageNameSuggestionCacheManager implements SynchronousResourceReloader {
    public static volatile HashMap<String, Suggestions> suggestionsCache = new HashMap<>();

    public PageNameSuggestionCacheManager() {
    }

    @Override
    public void reload(ResourceManager manager) {
        suggestionsCache.clear();
    }
}
