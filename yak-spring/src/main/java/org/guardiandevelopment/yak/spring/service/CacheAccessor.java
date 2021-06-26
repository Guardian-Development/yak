package org.guardiandevelopment.yak.spring.service;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;
import org.guardiandevelopment.yak.core.YakCache;
import org.springframework.stereotype.Service;

/**
 * Provides access to the caches configured within the server.
 * <p>
 *   Note: Synchronized used on methods, as this is just a test project for playing with Spring, and the cache is not
 *   thread safe.
 * </p>
 */
@Service
public class CacheAccessor {

  private final Map<String, YakCache<String, ByteBuffer>> availableCaches;

  CacheAccessor(final Map<String, YakCache<String, ByteBuffer>> availableCaches) {

    this.availableCaches = availableCaches;
  }

  /**
   * Get a copy of a value within a cache.
   * <p>
   * Note: this returns you a copy allocated for your request, so you may do what you like with this once you
   * have it.
   * </p>
   *
   * @param cacheName the cache to query
   * @param keyName   the key within the cache to query
   * @return the value in the cache, or empty if either the cache or key does not exist
   */
  public synchronized Optional<ByteBuffer> getValueInCache(final String cacheName, final String keyName) {
    if (!availableCaches.containsKey(cacheName)) {
      return Optional.empty();
    }

    final var cache = availableCaches.get(cacheName);
    final var result = cache.get(keyName);

    return Optional.ofNullable(result);
  }

  /**
   * Save a value within a cache.
   *
   * @param cacheName the cache to save the value in
   * @param keyName   the key to save the value under in the cache
   * @param value     the value to store
   * @return true if stored successfully, false if the cache doesnt exist or was unable to save
   */
  public synchronized boolean saveValueInCache(final String cacheName, final String keyName, final String value) {
    if (!availableCaches.containsKey(cacheName)) {
      return false;
    }

    final var cache = availableCaches.get(cacheName);
    final var keyAsBuffer = ByteBuffer.wrap(value.getBytes());

    return cache.put(keyName, keyAsBuffer);
  }
}
