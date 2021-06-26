package org.guardiandevelopment.yak.spring.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.guardiandevelopment.yak.core.events.YakEventListener;
import org.springframework.stereotype.Component;

/**
 * Custom metrics used by the application.
 */
@Component
public class YakCacheMetrics {

  private final MeterRegistry registry;

  YakCacheMetrics(final MeterRegistry registry) {

    this.registry = registry;
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
    registry.counter("cache_response_total",
            "cache_name", cacheName,
            "key", key,
            "response_type", responseType.name())
            .increment();
  }
}


