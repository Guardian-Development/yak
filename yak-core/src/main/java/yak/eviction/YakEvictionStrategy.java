package yak.eviction;

import yak.events.YakEventListener;

/**
 * Provides a method for evicting keys in the cache when required.
 *
 * @param <T> the key type of the cache
 */
public interface YakEvictionStrategy<T> extends YakEventListener<T> {

  /**
   * Called before the cache is created, allowing for any setup to be done before use.
   *
   * @param maximumKeys the maximum number of keys to be stored within the cache
   */
  void init(final int maximumKeys);

  /**
   * Provides the next key to evict when called. The cache will remove the returned key synchronously.
   *
   * @return the key to immediate evict
   */
  T keyToEvict();

  /**
   * Use a LRU based eviction strategy.
   *
   * @param <T> the key type of the cache
   * @return LRU eviction strategy
   */
  static <T> YakEvictionStrategy<T> leastRecentlyUsed() {
    return new LeastRecentlyUsedEvictionStrategy<>();
  }
}
