package org.guardiandev.yak.cacheprogression;

import org.agrona.concurrent.UnsafeBuffer;
import org.guardiandev.yak.YakCache;
import org.guardiandev.yak.YakCacheBuilder;
import org.guardiandev.yak.config.YakCacheConfig;
import org.guardiandev.yak.eviction.YakEvictionStrategy;
import org.guardiandev.yak.serialization.YakValueSerializer;
import org.guardiandev.yak.storage.YakValueStorage;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * example.
 */
public final class CacheInitializer {

  private final List<YakCacheConfig> config;

  public CacheInitializer(final List<YakCacheConfig> config) {
    this.config = config;
  }

  public Map<String, YakCache<String, ByteBuffer>> init() {

    final var caches = new HashMap<String, YakCache<String, ByteBuffer>>(config.size(), 1);

    for (final var cache : config) {
      caches.put(cache.getName(), buildFromConfig(cache));
    }

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
        return deserializeIntoBuffer;
      }
    });

    return builder.build();
  }
}
