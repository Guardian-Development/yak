package org.guardiandevelopment.yak.server.config;

import java.util.List;

/**
 * Config POJO for a yak server config.
 */
public final class YakServerConfig {

  private Integer port;
  private List<YakCacheConfig> caches;
  private YakMemoryPoolBufferConfig networkBufferPool;
  private YakMemoryPoolConfig httpRequestMemoryPool;
  private YakMemoryPoolBufferConfig incomingCacheRequestPool;
  private YakThreadIdleStrategy threadIdleStrategy;

  public List<YakCacheConfig> getCaches() {
    return caches;
  }

  public YakServerConfig setCaches(List<YakCacheConfig> caches) {
    this.caches = caches;
    return this;
  }

  public int getPort() {
    return port;
  }

  public YakServerConfig setPort(int port) {
    this.port = port;
    return this;
  }

  public YakMemoryPoolBufferConfig getNetworkBufferPool() {
    return networkBufferPool;
  }

  public YakServerConfig setNetworkBufferPool(YakMemoryPoolBufferConfig networkBufferPool) {
    this.networkBufferPool = networkBufferPool;
    return this;
  }

  public YakMemoryPoolConfig getHttpRequestMemoryPool() {
    return httpRequestMemoryPool;
  }

  public YakServerConfig setHttpRequestMemoryPool(YakMemoryPoolConfig httpRequestMemoryPool) {
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
}
