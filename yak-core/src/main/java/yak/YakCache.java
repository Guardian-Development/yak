package yak;

import yak.serialization.YakValueSerializer;
import yak.storage.YakValueStorage;

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
  private final int maximumKeys;

  YakCache(final int maximumKeys,
           final YakValueSerializer<Q> valueSerializer,
           final YakValueStorage storage) {
    this.maximumKeys = maximumKeys;
    this.valueSerializer = valueSerializer;
    this.storage = storage;
  }

  /**
   * Get a value from the cache, if it is present, else return null.
   *
   * @param key the key to lookup
   * @return the value associated with the key, or null if not found.
   */
  public Q get(final T key) {
    final var hashCode = key.hashCode();
    final var index = Math.abs(hashCode % maximumKeys);
    final var value = storage.getStorage(index);

    return valueSerializer.deserialize(value);
  }

  /**
   * Put a value into the cache for the given key.
   *
   * @param key   the key to associate the value with
   * @param value the value to store
   * @return true if successfully stored, else false.
   */
  public boolean put(final T key, final Q value) {
    final var hashCode = key.hashCode();
    final var index = Math.abs(hashCode % maximumKeys);
    final var storageValue = storage.getStorage(index);

    final var entrySize = valueSerializer.serialize(value, storageValue);
    return true;
  }
}
