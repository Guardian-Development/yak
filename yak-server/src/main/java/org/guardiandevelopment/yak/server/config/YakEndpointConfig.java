package org.guardiandevelopment.yak.server.config;

/**
 * Config POJO for a yak endpoint config.
 */
public final class YakEndpointConfig {

  private String healthCheck;
  private String cache;

  public String getHealthCheck() {
    return healthCheck;
  }

  public YakEndpointConfig setHealthCheck(String healthCheck) {
    this.healthCheck = healthCheck;
    return this;
  }

  public String getCache() {
    return cache;
  }

  public YakEndpointConfig setCache(String cache) {
    this.cache = cache;
    return this;
  }
}
