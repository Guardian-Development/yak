package org.guardiandevelopment.yak.server.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import org.guardiandevelopment.yak.core.events.YakEventListener;
import org.guardiandevelopment.yak.server.config.YakMetricsConfig;

/**
 * Metrics specific to monitoring caches.
 */
public final class CacheMetrics {

  private final Counter cacheResponseCounter;
  private final YakMetricsConfig config;

  CacheMetrics(final YakMetricsConfig config, final CollectorRegistry registry) {

    this.config = config;

    if (config.isEnabled()) {
      cacheResponseCounter = Counter.build()
              .name("cache_response_total")
              .labelNames("cache_name", "key", "response_type")
              .help("Cache Response total")
              .register(registry);
    } else {
      cacheResponseCounter = null;
    }
  }

  /**
   * Increment the counter for this cache, key, and response type.
   *
   * @param cacheName    the cache name
   * @param key          the key requested within the cache
   * @param responseType the response given by the cache
   */
  public void incCacheResponse(final String cacheName,
                               final String key,
                               final YakEventListener.YakEvent responseType) {
    if (config.isEnabled()) {
      cacheResponseCounter.labels(cacheName, key, responseType.name()).inc();
    }
  }
}
