package org.guardiandevelopment.yak.server.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.agrona.LangUtil;
import org.guardiandevelopment.yak.server.config.YakMetricsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used for controlling the prom metrics used by the server.
 */
public final class Metrics {

  private static final Logger LOG = LoggerFactory.getLogger(Metrics.class);

  private final YakMetricsConfig config;
  private final CollectorRegistry registry;
  private final ThreadMetrics threadMetrics;
  private final CacheMetrics cacheMetrics;

  private HTTPServer httpServer;

  /**
   * Creates a metrics configuration from the provided config.
   *
   * @param config the config to expose metrics over
   */
  public Metrics(final YakMetricsConfig config) {

    this.config = config;
    this.registry = new CollectorRegistry();
    this.threadMetrics = new ThreadMetrics(config, registry);
    this.cacheMetrics = new CacheMetrics(config, registry);
  }

  /**
   * Starts the metrics server on the configured port, if enabled.
   */
  public synchronized void start() {

    LOG.info("initialising server metrics");

    if (!config.isEnabled()) {
      LOG.info("metrics disabled for server");
      return;
    }

    DefaultExports.register(registry);

    try {
      httpServer = new HTTPServer(new InetSocketAddress(config.getPort()), registry);
    } catch (IOException e) {
      LOG.error("failed to initalise metrics for server", e);
      LangUtil.rethrowUnchecked(e);
    }

    LOG.info("metrics running on port {}", config.getPort());
  }

  /**
   * Stops the metrics server on shutdown.
   */
  public synchronized void stop() {
    if (config.isEnabled()) {
      LOG.info("stopping server metrics");

      httpServer.stop();

      LOG.info("stopped server metrics");
    }
  }

  public ThreadMetrics getThreadHeartbeatMetrics() {
    return threadMetrics;
  }

  public CacheMetrics getCacheMetrics() {
    return cacheMetrics;
  }
}
