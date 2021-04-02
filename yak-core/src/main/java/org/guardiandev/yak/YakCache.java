package org.guardiandev.yak;

import static org.guardiandev.yak.events.YakEventListener.YakEvent.GET_CACHE_HIT;
import static org.guardiandev.yak.events.YakEventListener.YakEvent.GET_CACHE_MISS;
import static org.guardiandev.yak.events.YakEventListener.YakEvent.PUT;

import java.util.List;
import org.guardiandev.yak.events.YakEventListener;
import org.guardiandev.yak.eviction.YakEvictionStrategy;
import org.guardiandev.yak.serialization.YakValueSerializer;
import org.guardiandev.yak.storage.OpenAddressingHashMap;
import org.guardiandev.yak.storage.YakValueStorage;

/**
 * A key-value cache, using the {@link #storage} to store the values in the cache.
 *
 * @param <T> the key type
 * @param <Q> the value type
 */
public final class YakCache<T, Q> {

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

  private final YakValueStorage storage;
  private final YakValueSerializer<Q> valueSerializer;
  private final OpenAddressingHashMap<T> keyToStorageIndex;
  private final YakEvictionStrategy<T> evictionStrategy;
  private final List<YakEventListener<T>> eventListeners;

  YakCache(final int maximumKeys,
           final YakValueSerializer<Q> valueSerializer,
           final YakValueStorage storage,
           final YakEvictionStrategy<T> evictionStrategy,
           final List<YakEventListener<T>> eventListeners) {
    this.valueSerializer = valueSerializer;
    this.storage = storage;
    this.keyToStorageIndex = new OpenAddressingHashMap<>(maximumKeys);
    this.evictionStrategy = evictionStrategy;
    this.eventListeners = eventListeners;
  }

  /**
   * Get a value from the cache, if it is present, else return null.
   *
   * @param key the key to lookup
   * @return the value associated with the key, or null if not found.
   */
  public Q get(final T key) {
    final var storageIndex = keyToStorageIndex.getExistingOrAssign(key);
    if (storageIndex == null) {
      broadcastEvent(GET_CACHE_MISS, key, null);
      return null;
    }

    final var storageValue = storage.getStorage(storageIndex);
    final var value = valueSerializer.deserialize(storageValue);

    broadcastEvent(GET_CACHE_HIT, key, value);

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
    var storageIndex = keyToStorageIndex.getExistingOrAssign(key);
    if (storageIndex == null) {
      final var evictedKey = evictionStrategy.keyToEvict();
      keyToStorageIndex.delete(evictedKey);

      storageIndex = keyToStorageIndex.getExistingOrAssign(key);
    }

    final var storageValue = storage.getStorage(storageIndex);
    valueSerializer.serialize(value, storageValue);

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
}
