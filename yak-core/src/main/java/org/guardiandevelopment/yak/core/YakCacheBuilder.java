package org.guardiandevelopment.yak.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.guardiandevelopment.yak.core.events.YakEventListener;
import org.guardiandevelopment.yak.core.eviction.YakEvictionStrategy;
import org.guardiandevelopment.yak.core.serialization.YakValueSerializer;
import org.guardiandevelopment.yak.core.storage.YakValueStorage;
import org.guardiandevelopment.yak.core.utils.IntegerExtensions;

/**
 * Used to help build a cache, with default parameters used for all not-provided user values.
 *
 * @param <T> the key type of the cache
 * @param <Q> the value type of the cache
 */
public final class YakCacheBuilder<T, Q> {

  private String name = String.format("cache-%s", UUID.randomUUID().toString().substring(0, 5));
  private int maximumKeys = 1024;
  private int fixedValueSize = 256;
  private YakValueStorage storage = YakValueStorage.DIRECT_MEMORY_STORAGE;
  private YakEvictionStrategy<T> strategy = YakEvictionStrategy.leastRecentlyUsed();
  private YakValueSerializer<Q> valueSerializer;
  private List<YakEventListener<T>> eventListeners = new ArrayList<>();

  /**
   * New builder for creating a Yak Cache.
   */
  public YakCacheBuilder() {
  }

  /**
   * Sets the maximum number of keys in the cache at any point. If the key size is hit, the cache will evict keys
   * to make sure it does not exceed this limit.
   *
   * <p>
   * By default, the maximum keys in the cache is 1024.
   * </p>
   *
   * @param maximumKeys the maximum number of keys
   * @return self
   */
  public YakCacheBuilder<T, Q> maximumKeys(final int maximumKeys) {
    assert IntegerExtensions.isPowerOf2(maximumKeys) : "the maximum keys in the cache must be a power of 2";

    this.maximumKeys = maximumKeys;
    return this;
  }

  /**
   * The maximum size, in bytes, of a value stored in the cache. This is used when provisioning the storage for the
   * cache.
   * <p>
   * By default, the maximum size of a key in the cache is 256 bytes.
   * </p>
   *
   * @param fixedValueSize the maximum size of a value in the ache in bytes
   * @return self
   */
  public YakCacheBuilder<T, Q> fixedValueSize(final int fixedValueSize) {
    this.fixedValueSize = fixedValueSize;
    return this;
  }

  /**
   * Used for serializing the values to and from the storage mechanism.
   *
   * @param valueSerializer serializer for your value type
   * @return self
   */
  public YakCacheBuilder<T, Q> valueSerializer(final YakValueSerializer<Q> valueSerializer) {
    this.valueSerializer = valueSerializer;
    return this;
  }

  /**
   * The underlying storage mechanism when storing values in the cache.
   * <p>
   * By default, the storage mechanism for the cache is in-memory, meaning all values are stored in RAM.
   * </p>
   *
   * @param storage the storage mechanism to use
   * @return self
   */
  public YakCacheBuilder<T, Q> valueStorageMechanism(final YakValueStorage storage) {
    this.storage = storage;
    return this;
  }

  /**
   * The eviction strategy to use with the cache.
   * <p>
   * By default, the eviction strategy is an LRU cache.
   * </p>
   *
   * @param strategy the eviction strategy to use
   * @return self
   */
  public YakCacheBuilder<T, Q> evictionStrategy(final YakEvictionStrategy<T> strategy) {

    this.strategy = strategy;
    return this;
  }

  /**
   * Register an event listener to be notified of events within the cache.
   *
   * @param listener the listener
   * @return self
   */
  public YakCacheBuilder<T, Q> eventListener(final YakEventListener<T> listener) {

    this.eventListeners.add(listener);
    return this;
  }

  /**
   * The name of the cache.
   * <p>
   * By default, this has the format cache-UUID. This name is used within all logs, when enabled.
   * </p>
   *
   * @param name the name of the cache
   * @return self
   */
  public YakCacheBuilder<T, Q> name(final String name) {

    this.name = name;
    return this;
  }

  /**
   * Build the cache, and init any storage resources needed.
   * <p>
   * After this method returns the cache will be created and available for use.
   * </p>
   *
   * @return the built cache
   */
  public YakCache<T, Q> build() {
    storage.init(maximumKeys, fixedValueSize);
    strategy.init(maximumKeys);

    eventListeners.add(strategy);
    final var unmodifiableListeners = Collections.unmodifiableList(eventListeners);

    return new YakCache<>(name, maximumKeys, valueSerializer, storage, strategy, unmodifiableListeners);
  }
}