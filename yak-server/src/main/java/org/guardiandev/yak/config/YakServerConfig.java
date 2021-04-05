package org.guardiandev.yak.config;

import java.util.List;

final class YakServerConfig {

  private Integer port;
  private List<YakCacheConfig> caches;

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
}
