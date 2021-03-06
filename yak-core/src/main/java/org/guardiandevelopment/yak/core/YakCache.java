package org.guardiandevelopment.yak.core;

import static org.guardiandevelopment.yak.core.events.YakEventListener.YakEvent.GET_CACHE_HIT;
import static org.guardiandevelopment.yak.core.events.YakEventListener.YakEvent.GET_CACHE_MISS;
import static org.guardiandevelopment.yak.core.events.YakEventListener.YakEvent.PUT;

import java.util.List;
import java.util.Objects;
import org.guardiandevelopment.yak.core.events.YakEventListener;
import org.guardiandevelopment.yak.core.eviction.YakEvictionStrategy;
import org.guardiandevelopment.yak.core.serialization.YakValueSerializer;
import org.guardiandevelopment.yak.core.storage.OpenAddressingHashMap;
import org.guardiandevelopment.yak.core.storage.YakValueStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A key-value cache, using the {@link #storage} to store the values in the cache.
 *
 * @param <T> the key type
 * @param <Q> the value type
 */
public final class YakCache<T, Q> {

  private static final Logger LOG = LoggerFactory.getLogger(YakCache.class);

  /**
   * Builder for creating a new cache.
   *
   * @param <T> the key type
   * @param <Q> the value type
   * @return builder
   */
  public static <T, Q> YakCacheBuilder<T, Q> newBuilder() {
    return new YakCacheBuilder<>();
  }

  private final String name;
  private final YakValueStorage storage;
  private final YakValueSerializer<Q> valueSerializer;
  private final OpenAddressingHashMap<T> keyToStorageIndex;
  private final YakEvictionStrategy<T> evictionStrategy;
  private final List<YakEventListener<T>> eventListeners;

  YakCache(final String name,
           final int maximumKeys,
           final YakValueSerializer<Q> valueSerializer,
           final YakValueStorage storage,
           final YakEvictionStrategy<T> evictionStrategy,
           final List<YakEventListener<T>> eventListeners) {
    this.name = name;
    this.valueSerializer = valueSerializer;
    this.storage = storage;
    this.keyToStorageIndex = new OpenAddressingHashMap<>(maximumKeys);
    this.evictionStrategy = evictionStrategy;
    this.eventListeners = eventListeners;

    LOG.debug("[cacheName={}] cache created with maximum keys {}, eviction strategy {}, and storage {}",
            name, maximumKeys, evictionStrategy, storage);
  }

  /**
   * Get a value from the cache, if it is present, else return null.
   *
   * @param key the key to lookup
   * @return the value associated with the key, or null if not found.
   */
  public Q get(final T key) {
    LOG.trace("[cacheName={},key={}] getting entry in cache", name, key);

    final var storageIndex = keyToStorageIndex.get(key);

    if (storageIndex == null) {
      LOG.trace("[cacheName={},key={}] cache miss", name, key);
      broadcastEvent(GET_CACHE_MISS, key, null);
      return null;
    }

    final var storageValue = storage.getStorage(storageIndex);
    final var value = valueSerializer.deserialize(storageValue);

    broadcastEvent(GET_CACHE_HIT, key, value);

    LOG.trace("[cacheName={},key={}] cache hit, returning deserialized value {}", name, key, value);

    return value;
  }

  /**
   * Put a value into the cache for the given key.
   *
   * @param key   the key to associate the value with
   * @param value the value to store
   * @return true if successfully stored, else false.
   */
  public boolean put(final T key, final Q value) {
    LOG.trace("[cacheName={},key={}] putting entry in cache", name, key);

    var storageIndex = keyToStorageIndex.getExistingOrAssign(key);
    if (storageIndex == null) {
      LOG.trace("[cacheName={},key={}] cache full, triggering eviction", name, key);
      final var evictedKey = evictionStrategy.keyToEvict();
      keyToStorageIndex.delete(evictedKey);

      LOG.trace("[cacheName={},key={}] evicting key {}", name, key, evictedKey);
      storageIndex = keyToStorageIndex.getExistingOrAssign(key);
    }

    final var storageValue = storage.getStorage(storageIndex);
    valueSerializer.serialize(value, storageValue);

    LOG.trace("[cacheName={},key={}] entry added to cache", name, key);
    broadcastEvent(PUT, key, value);

    return true;
  }

  private void broadcastEvent(final YakEventListener.YakEvent event, final T key, final Q value) {
    if (eventListeners.isEmpty()) {
      return;
    }

    for (var i = 0; i < eventListeners.size(); i++) {
      eventListeners.get(i).accept(event, key, value);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    YakCache<?, ?> yakCache = (YakCache<?, ?>) o;
    return name.equals(yakCache.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String toString() {
    return "YakCache{"
            + "name='"
            + name
            + '\''
            + ", storage="
            + storage
            + ", valueSerializer="
            + valueSerializer
            + ", keyToStorageIndex="
            + keyToStorageIndex
            + ", evictionStrategy="
            + evictionStrategy
            + '}';
  }
}
