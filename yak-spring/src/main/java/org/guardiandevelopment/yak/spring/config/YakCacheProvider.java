package org.guardiandevelopment.yak.spring.config;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.agrona.concurrent.UnsafeBuffer;
import org.guardiandevelopment.yak.core.YakCache;
import org.guardiandevelopment.yak.core.YakCacheBuilder;
import org.guardiandevelopment.yak.core.eviction.YakEvictionStrategy;
import org.guardiandevelopment.yak.core.serialization.YakValueSerializer;
import org.guardiandevelopment.yak.core.storage.YakValueStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class YakCacheProvider {

  private static final Logger LOG = LoggerFactory.getLogger(YakCacheProvider.class);

  private final YakServerConfig yakServerConfig;
  private final YakCacheMetrics cacheMetrics;

  YakCacheProvider(final YakServerConfig yakServerConfig, final YakCacheMetrics cacheMetrics) {

    this.yakServerConfig = yakServerConfig;
    this.cacheMetrics = cacheMetrics;
  }

  @Bean
  public Map<String, YakCache<String, ByteBuffer>> caches() {
    final var caches = yakServerConfig.getCaches();

    final var cacheMap = new HashMap<String, YakCache<String, ByteBuffer>>();
    for (final var cache : caches) {
      final var cacheFromConfig = buildFromConfig(cache);
      cacheMap.put(cache.getName(), cacheFromConfig);
    }

    LOG.info("caches available on server: {}", caches.stream().map(YakCacheConfig::getName).collect(Collectors.toList()));

    return cacheMap;
  }

  private YakCache<String, ByteBuffer> buildFromConfig(final YakCacheConfig cache) {
    final var builder = new YakCacheBuilder<String, ByteBuffer>();
    builder.maximumKeys(cache.getMaximumKeys());
    builder.fixedValueSize(cache.getFixedValueSize());
    builder.evictionStrategy(YakEvictionStrategy.leastRecentlyUsed());
    builder.valueStorageMechanism(YakValueStorage.DIRECT_MEMORY_STORAGE);
    builder.name(cache.getName());
    builder.eventListener((event, key, value) -> cacheMetrics.incCacheResponse(cache.getName(), key, event));
    builder.valueSerializer(new YakValueSerializer<>() {

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
        final var bufferLength = valueBuffer.getInt(0);

        // allocates a buffer per read, to avoid multi threading issues after reading from the cache
        final var resultCopy = ByteBuffer.allocate(bufferLength);
        valueBuffer.getBytes(Integer.BYTES, resultCopy, bufferLength);
        resultCopy.flip();
        return resultCopy;
      }
    });

    return builder.build();
  }
}
