package org.guardiandev.yak.events;

/**
 * Provides a mechanism for listening to events occurring within the cache in a synchronous context.
 *
 * @param <T> the key type of the cache
 */
public interface YakEventListener<T> {

  /**
   * The type of operation that triggered the event.
   */
  enum YakEvent {
    GET_CACHE_HIT,
    GET_CACHE_MISS,
    PUT
  }

  /**
   * Called when an event within the cache occurs.
   * <p>
   * this method executes synchronously with the event, so any implementer should avoid expensive operations
   * when implementing this interface.
   * </p>
   *
   * @param event the type of event the cache has executed
   * @param key   the key of the entry in the cache
   * @param value the value in the cache, possibly null
   */
  void accept(final YakEvent event, final T key, final Object value);
}
