package org.guardiandev.yak.core.eviction;

import java.util.LinkedHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static final Logger LOG = LoggerFactory.getLogger(LeastRecentlyUsedEvictionStrategy.class);

  private LinkedHashSet<T> keys;

  @Override
  public void init(int maximumKeys) {
    LOG.trace("[strategy={}] initialising with maximum keys {}", this, maximumKeys);
    keys = new LinkedHashSet<>(maximumKeys, 1f);
  }

  @Override
  public T keyToEvict() {
    final var lastUsed = keys.iterator().next();
    keys.remove(lastUsed);
    LOG.trace("[strategy={},key={}] evicted key", this, lastUsed);
    return lastUsed;
  }

  @Override
  public void accept(final YakEvent event, final T key, final Object value) {
    LOG.trace("[strategy={},key={},event={}] received event", this, key, event);

    if (event == YakEvent.GET_CACHE_MISS) {
      LOG.trace("[strategy={},key={},event={}] ignoring cache miss event", this, key, event);
      return;
    }

    keys.remove(key);
    keys.add(key);

    LOG.trace("[strategy={},key={},event={}] marked key as used", this, key, event);
  }

  @Override
  public String toString() {
    return "LeastRecentlyUsedEvictionStrategy{}";
  }
}
