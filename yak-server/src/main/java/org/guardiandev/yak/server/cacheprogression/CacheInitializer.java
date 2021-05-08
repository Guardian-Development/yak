package org.guardiandev.yak.server.cacheprogression;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.agrona.concurrent.UnsafeBuffer;
import org.guardiandev.yak.core.YakCache;
import org.guardiandev.yak.core.YakCacheBuilder;
import org.guardiandev.yak.core.eviction.YakEvictionStrategy;
import org.guardiandev.yak.core.serialization.YakValueSerializer;
import org.guardiandev.yak.core.storage.YakValueStorage;
import org.guardiandev.yak.server.acceptor.IncomingCacheRequest;
import org.guardiandev.yak.server.config.YakCacheConfig;
import org.guardiandev.yak.server.pool.MemoryPool;
import org.guardiandev.yak.server.responder.CacheResponseToResponderBridge;


/**
 * Responsible for initializing the caches from config.
 * <p>
 * this adds a further cache wrapper for the key of {@link #NULL_CACHE_RESPONDER_KEY} which can be used to handle
 * any requests for caches that do not exist.
 * </p>
 */
public final class CacheInitializer {

  public static String NULL_CACHE_RESPONDER_KEY = "null_cache_wrapper";

  private final List<YakCacheConfig> config;

  public CacheInitializer(final List<YakCacheConfig> config) {
    this.config = config;
  }

  /**
   * Initalise the cache wrappers, assigning the name of the cache to wrapper.
   * <p>
   * this also inserts a cache wrapper at the {@link #NULL_CACHE_RESPONDER_KEY} key, which will handle any
   * requests that are for a non-existent cache.
   * </p>
   *
   * @param responderBridge          where caches should send results after execution
   * @param incomingCacheRequestPool the pool to return cache requests to when they have been processed
   * @return cache name to cache wrapper
   */
  public Map<String, CacheWrapper> init(final CacheResponseToResponderBridge responderBridge,
                                        final MemoryPool<IncomingCacheRequest> incomingCacheRequestPool) {

    final var caches = new HashMap<String, CacheWrapper>(config.size(), 1);

    for (final var cache : config) {
      final var builtCache = buildFromConfig(cache);
      final var wrap = new CacheWrapper(cache.getName(), builtCache, responderBridge, incomingCacheRequestPool);
      caches.put(cache.getName(), wrap);
    }

    caches.put(NULL_CACHE_RESPONDER_KEY, new CacheWrapper("not-found-cache", null, responderBridge, incomingCacheRequestPool));

    return caches;
  }

  private YakCache<String, ByteBuffer> buildFromConfig(final YakCacheConfig cache) {
    final var builder = new YakCacheBuilder<String, ByteBuffer>();
    builder.maximumKeys(cache.getMaximumKeys());
    builder.fixedValueSize(cache.getFixedValueSize());
    builder.evictionStrategy(YakEvictionStrategy.leastRecentlyUsed());
    builder.valueStorageMechanism(YakValueStorage.DIRECT_MEMORY_STORAGE);
    builder.name(cache.getName());
    builder.valueSerializer(new YakValueSerializer<>() {

      private final ByteBuffer deserializeIntoBuffer = ByteBuffer.allocateDirect(cache.getFixedValueSize());

      @Override
      public int serialize(final ByteBuffer value, final UnsafeBuffer valueBuffer) {
        final var bufferLength = value.remaining();

        var bytesWritten = 0;
        valueBuffer.putInt(0, bufferLength);
        bytesWritten += Integer.BYTES;

        valueBuffer.putBytes(bytesWritten, value, bufferLength);
        bytesWritten += bufferLength;

        return bytesWritten;
      }

      @Override
      public ByteBuffer deserialize(final UnsafeBuffer valueBuffer) {
        deserializeIntoBuffer.clear();

        final var bufferLength = valueBuffer.getInt(0);
        valueBuffer.getBytes(Integer.BYTES, deserializeIntoBuffer, bufferLength);
        deserializeIntoBuffer.flip();
        return deserializeIntoBuffer;
      }
    });

    return builder.build();
  }
}
