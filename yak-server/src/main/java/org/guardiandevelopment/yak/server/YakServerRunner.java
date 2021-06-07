package org.guardiandevelopment.yak.server;

import java.nio.file.Path;
import java.util.stream.Collectors;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.guardiandevelopment.yak.server.acceptor.ConnectionAcceptorThread;
import org.guardiandevelopment.yak.server.acceptor.HttpRequestProcessor;
import org.guardiandevelopment.yak.server.acceptor.IncomingConnectionFactory;
import org.guardiandevelopment.yak.server.cacheprogression.CacheInitializer;
import org.guardiandevelopment.yak.server.cacheprogression.CacheProgressionThread;
import org.guardiandevelopment.yak.server.cacheprogression.IncomingConnectionToCacheWrapperBridge;
import org.guardiandevelopment.yak.server.config.YakCacheConfig;
import org.guardiandevelopment.yak.server.config.YakConfigFromJsonBuilder;
import org.guardiandevelopment.yak.server.config.YakServerConfig;
import org.guardiandevelopment.yak.server.metrics.Metrics;
import org.guardiandevelopment.yak.server.pool.Factory;
import org.guardiandevelopment.yak.server.responder.ResponderBridge;
import org.guardiandevelopment.yak.server.responder.ResponderThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for initialising and starting the application.
 */
public final class YakServerRunner {

  private static final Logger LOG = LoggerFactory.getLogger(YakServerRunner.class);

  private final YakServerConfig config;

  private ConnectionAcceptorThread acceptorThread;
  private CacheProgressionThread cacheProgressionThread;
  private ResponderThread responderThread;
  private Metrics metrics;

  /**
   * Initialise the server to be ran with the passed in config.
   *
   * @param config the config to run the server with
   */
  public YakServerRunner(final YakServerConfig config) {
    this.config = config;
  }

  /**
   * Initialise all resources for the server, but does not run the application.
   *
   * @return true if initialised successfully, else false
   */
  public boolean init() {

    LOG.info("initialising server from config");

    metrics = new Metrics(config.getMetricsConfig());

    final var networkBufferPool = Factory.networkBufferPool(
            config.getNetworkBufferPool().getPoolSize(),
            config.getNetworkBufferPool().getBufferSize(),
            config.getNetworkBufferPool().isFillOnCreation());
    final var httpRequestMemoryPool = Factory.httpRequestPool(
            config.getHttpRequestMemoryPool().getPoolSize(),
            config.getHttpRequestMemoryPool().getBufferSize(),
            config.getHttpRequestMemoryPool().isFillOnCreation());
    final var incomingCacheRequestMemoryPool = Factory.incomingCacheRequestPool(
            config.getIncomingCacheRequestPool().getPoolSize(),
            config.getIncomingCacheRequestPool().getBufferSize(),
            config.getIncomingCacheRequestPool().isFillOnCreation());

    final var threadIdleStrategy = new BackoffIdleStrategy(
            config.getThreadIdleStrategy().getMaxSpins(),
            config.getThreadIdleStrategy().getMaxYields(),
            config.getThreadIdleStrategy().getMinParkPeriodNs(),
            config.getThreadIdleStrategy().getMaxParkPeriodNs());

    responderThread = new ResponderThread(threadIdleStrategy, metrics.getThreadHeartbeatMetrics());

    final var responseBridge = new ResponderBridge(responderThread);
    final var cacheInit = new CacheInitializer(config.getCaches(), metrics.getCacheMetrics());
    final var cacheNameToCache = cacheInit.init(responseBridge, incomingCacheRequestMemoryPool);

    LOG.info("caches available on server: {}", config.getCaches().stream().map(YakCacheConfig::getName).collect(Collectors.toList()));

    final var connectionFactory = new IncomingConnectionFactory(networkBufferPool, httpRequestMemoryPool);
    final var connectionCacheBridge = new IncomingConnectionToCacheWrapperBridge(cacheNameToCache);
    final var httpRequestProcessor = new HttpRequestProcessor(
            config.getEndpointConfig(), connectionCacheBridge, responseBridge, networkBufferPool, incomingCacheRequestMemoryPool);

    acceptorThread = new ConnectionAcceptorThread(
            config.getPort(),
            connectionFactory,
            httpRequestProcessor,
            threadIdleStrategy,
            metrics.getThreadHeartbeatMetrics());

    cacheProgressionThread = new CacheProgressionThread(
            cacheNameToCache.values(),
            threadIdleStrategy,
            metrics.getThreadHeartbeatMetrics());

    LOG.info("initialised server from config");

    return true;
  }

  /**
   * Start the server, requires {@link #init()} to be called first.
   */
  public void start() {
    LOG.info("starting server");

    metrics.start();
    responderThread.start();
    cacheProgressionThread.start();
    acceptorThread.start();

    Runtime.getRuntime().addShutdownHook(new Thread(YakServerRunner.this::stop, "shutdown-thread"));

    LOG.info("[hostName={},port={}] server started",
            acceptorThread.getListeningOnAddress().getHostName(),
            acceptorThread.getListeningOnAddress().getPort());
  }

  /**
   * Whether all threads within the server are running and alive.
   *
   * @return true if running an healthy, else false.
   */
  public boolean isRunning() {
    return acceptorThread.isAlive() && cacheProgressionThread.isAlive() && responderThread.isAlive();
  }

  /**
   * Stop all threads within the server.
   */
  public void stop() {
    LOG.info("stopping server");

    acceptorThread.interrupt();
    cacheProgressionThread.interrupt();
    responderThread.interrupt();
    metrics.stop();

    LOG.info("server stopped");
  }

  /**
   * Entrypoint to the application, expects the location of the config file to passed as the first parameter.
   *
   * @param args command line args, first argument should be file path of config file
   */
  public static void main(final String[] args) {
    final var configLocation = Path.of(args[0]);
    final var config = new YakConfigFromJsonBuilder(configLocation).load();

    final var runner = new YakServerRunner(config);
    runner.init();
    runner.start();
  }
}
