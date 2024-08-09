package wiki.minecraft.heywiki.resource;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import wiki.minecraft.heywiki.wiki.PageExcerpt;

import java.util.HashMap;

/**
 * Manages the cache of page excerpts so that they can be reloaded.
 */
public class PageExcerptCacheManager implements SynchronousResourceReloader {
    public static volatile HashMap<String, PageExcerpt> excerptCache = new HashMap<>();

    public PageExcerptCacheManager() {
    }

    @Override
    public void reload(ResourceManager manager) {
        excerptCache.clear();
    }
}
