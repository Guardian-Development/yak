package org.guardiandevelopment.yak.server.config;

import java.util.List;

/**
 * Config POJO for a yak server config.
 */
public final class YakServerConfig {

  private Integer port;
  private YakEndpointConfig endpointConfig;
  private List<YakCacheConfig> caches;
  private YakMemoryPoolBufferConfig networkBufferPool;
  private YakMemoryPoolBufferConfig httpRequestMemoryPool;
  private YakMemoryPoolBufferConfig incomingCacheRequestPool;
  private YakThreadIdleStrategy threadIdleStrategy;
  private YakMetricsConfig metricsConfig;

  public int getPort() {
    return port;
  }

  public YakServerConfig setPort(int port) {
    this.port = port;
    return this;
  }

  public YakEndpointConfig getEndpointConfig() {
    return endpointConfig;
  }

  public YakServerConfig setEndpointConfig(YakEndpointConfig endpointConfig) {
    this.endpointConfig = endpointConfig;
    return this;
  }

  public List<YakCacheConfig> getCaches() {
    return caches;
  }

  public YakServerConfig setCaches(List<YakCacheConfig> caches) {
    this.caches = caches;
    return this;
  }

  public YakMemoryPoolBufferConfig getNetworkBufferPool() {
    return networkBufferPool;
  }

  public YakServerConfig setNetworkBufferPool(YakMemoryPoolBufferConfig networkBufferPool) {
    this.networkBufferPool = networkBufferPool;
    return this;
  }

  public YakMemoryPoolBufferConfig getHttpRequestMemoryPool() {
    return httpRequestMemoryPool;
  }

  public YakServerConfig setHttpRequestMemoryPool(YakMemoryPoolBufferConfig httpRequestMemoryPool) {
    this.httpRequestMemoryPool = httpRequestMemoryPool;
    return this;
  }

  public YakMemoryPoolBufferConfig getIncomingCacheRequestPool() {
    return incomingCacheRequestPool;
  }

  public YakServerConfig setIncomingCacheRequestPool(YakMemoryPoolBufferConfig incomingCacheRequestPool) {
    this.incomingCacheRequestPool = incomingCacheRequestPool;
    return this;
  }

  public YakThreadIdleStrategy getThreadIdleStrategy() {
    return threadIdleStrategy;
  }

  public YakServerConfig setThreadIdleStrategy(YakThreadIdleStrategy threadIdleStrategy) {
    this.threadIdleStrategy = threadIdleStrategy;
    return this;
  }

  public YakMetricsConfig getMetricsConfig() {
    return metricsConfig;
  }

  public YakServerConfig setMetricsConfig(YakMetricsConfig metricsConfig) {
    this.metricsConfig = metricsConfig;
    return this;
  }
}
