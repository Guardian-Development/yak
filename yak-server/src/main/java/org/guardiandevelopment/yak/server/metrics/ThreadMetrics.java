package org.guardiandevelopment.yak.server.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import org.guardiandevelopment.yak.server.config.YakMetricsConfig;

/**
 * Metrics specific to monitoring threads.
 */
public final class ThreadMetrics {

  private final Counter threadHeartbeatCounter;
  private final YakMetricsConfig config;

  ThreadMetrics(final YakMetricsConfig config, final CollectorRegistry registry) {

    this.config = config;

    if (config.isEnabled()) {
      threadHeartbeatCounter = Counter.build()
              .name("thread_heartbeat_total")
              .labelNames("thread_name")
              .help("Thread heartbeat total")
              .register(registry);
    } else {
      threadHeartbeatCounter = null;
    }
  }

  /**
   * Increment the heartbeat metric of the thread this method is called from.
   */
  public void incHeartbeat() {
    if (config.isEnabled()) {
      final var name = Thread.currentThread().getName();
      threadHeartbeatCounter.labels(name).inc();
    }
  }
}
