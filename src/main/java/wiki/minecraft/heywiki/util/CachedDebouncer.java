package wiki.minecraft.heywiki.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * A debouncer that caches the result of a callable for a certain amount of time.
 *
 * @param <Key>   The key type.
 * @param <Value> The value type.
 * @see Cache
 */
public class CachedDebouncer<Key, Value> {
    private static final Logger LOGGER = LogUtils.getLogger();
    /**
     * The timeout in milliseconds.
     */
    public final long timeoutMillis;
    private final Cache<Key, Value> cache = CacheBuilder.newBuilder()
                                                        .maximumSize(100)
                                                        .expireAfterAccess(10, TimeUnit.MINUTES)
                                                        .build();
    private volatile Key lastInput;

    /**
     * Creates a new debouncer with the given timeout.
     *
     * @param timeoutMillis The timeout in milliseconds.
     */
    public CachedDebouncer(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    /**
     * Gets the value for the given key, using the provider if the value is not cached.
     *
     * @param key      The key to get the value for.
     * @param provider The provider to use if the value is not cached.
     * @return The value, or an empty optional if the value is not cached.
     * @throws ExecutionException If the provider throws an exception.
     */
    public Optional<Value> get(Key key, Callable<Value> provider) throws ExecutionException {
        this.lastInput = key;

        var value = cache.getIfPresent(key);
        if (value != null) {
            return Optional.of(value);
        }

        try {
            Thread.sleep(timeoutMillis);
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted while waiting for debounce", e);
        }

        if (!key.equals(lastInput)) {
            return Optional.empty();
        }

        return Optional.of(cache.get(key, provider));
    }
}
