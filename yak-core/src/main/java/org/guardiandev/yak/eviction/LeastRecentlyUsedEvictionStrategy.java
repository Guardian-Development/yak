package org.guardiandev.yak.eviction;

import java.util.LinkedHashSet;

/**
 * LRU eviction strategy.
 *
 * <p>
 * Evicts the least recently seen key, ignoring events of type {@link YakEvent} GET_CACHE_MISS when marking
 * a key as used.
 * </p>
 *
 * @param <T> the key type of the cache
 */
public final class LeastRecentlyUsedEvictionStrategy<T> implements YakEvictionStrategy<T> {

  private LinkedHashSet<T> keys;

  @Override
  public void init(int maximumKeys) {
    keys = new LinkedHashSet<>(maximumKeys, 1f);
  }

  @Override
  public T keyToEvict() {
    final var lastUsed = keys.iterator().next();
    keys.remove(lastUsed);
    return lastUsed;
  }

  @Override
  public void accept(final YakEvent event, final T key, final Object value) {
    if (event == YakEvent.GET_CACHE_MISS) {
      return;
    }

    keys.remove(key);
    keys.add(key);
  }
}
