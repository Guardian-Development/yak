package org.guardiandevelopment.yak.server.config;

/**
 * Config POJO for a prometheus server metrics.
 */
public final class YakMetricsConfig {

  private boolean enabled;
  private int port;

  public boolean isEnabled() {
    return enabled;
  }

  public YakMetricsConfig setEnabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public int getPort() {
    return port;
  }

  public YakMetricsConfig setPort(int port) {
    this.port = port;
    return this;
  }
}
