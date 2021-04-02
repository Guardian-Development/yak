package org.guardiandev.yak.storage;

import org.agrona.concurrent.UnsafeBuffer;

/**
 * Provides storage for values in memory, exposed via an {@link UnsafeBuffer}.
 */
public interface YakValueStorage {

  /**
   * Provide any startup operations, such as pre-allocating memory, before the cache is made available for use.
   * <p>
   * This is called at build time of the cache, before the cache performs any further operations.
   * Implementations should consider this method blocking, and not thread-safe.
   * </p>
   *
   * @param maximumKeys    the maximum number of keys in the cache
   * @param fixedValueSize the maximum size to allocate, in bytes, for each value in the cache.
   */
  void init(final int maximumKeys, final int fixedValueSize);

  /**
   * Get the associated buffer for the value at the index.
   * <p>
   * This buffer is used to read/write values, and should be limited to the size of a single value.
   * Implementations should expect concurrent calls to this method for a given index.
   * </p>
   *
   * @param index the index of the requested storage
   * @return a buffer containing the storage area for the given index
   */
  UnsafeBuffer getStorage(final int index);

  /**
   * Store values in-memory.
   */
  YakValueStorage DIRECT_MEMORY_STORAGE = new DirectMemoryStorage();
}
