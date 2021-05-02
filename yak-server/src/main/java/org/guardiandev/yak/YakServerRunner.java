package org.guardiandev.yak;

import java.nio.file.Path;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.guardiandev.yak.acceptor.ConnectionAcceptorThread;
import org.guardiandev.yak.acceptor.IncomingConnectionFactory;
import org.guardiandev.yak.cacheprogression.CacheInitializer;
import org.guardiandev.yak.cacheprogression.CacheProgressionThread;
import org.guardiandev.yak.cacheprogression.IncomingConnectionToCacheWrapperBridge;
import org.guardiandev.yak.config.YakConfigFromJsonBuilder;
import org.guardiandev.yak.config.YakServerConfig;
import org.guardiandev.yak.pool.Factory;
import org.guardiandev.yak.responder.CacheResponseToResponderBridge;
import org.guardiandev.yak.responder.ResponderThread;

/**
 * Responsible for initialising and starting the application.
 */
public final class YakServerRunner {

  private final YakServerConfig config;

  private ConnectionAcceptorThread acceptorThread;
  private CacheProgressionThread cacheProgressionThread;
  private ResponderThread responderThread;

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
    final var networkBufferPool = Factory.networkBufferPool(
            config.getNetworkBufferPool().getPoolSize(),
            config.getNetworkBufferPool().getBufferSize(),
            config.getNetworkBufferPool().isFillOnCreation());
    final var httpRequestMemoryPool = Factory.httpRequestPool(
            config.getHttpRequestMemoryPool().getPoolSize(),
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

    responderThread = new ResponderThread(threadIdleStrategy);

    final var cacheResponseBridge = new CacheResponseToResponderBridge(responderThread);
    final var cacheInit = new CacheInitializer(config.getCaches());
    final var cacheNameToCache = cacheInit.init(cacheResponseBridge, incomingCacheRequestMemoryPool);

    final var connectionFactory = new IncomingConnectionFactory(networkBufferPool, httpRequestMemoryPool, incomingCacheRequestMemoryPool);
    final var connectionCacheBridge = new IncomingConnectionToCacheWrapperBridge(cacheNameToCache);

    acceptorThread = new ConnectionAcceptorThread(config.getPort(), connectionFactory, connectionCacheBridge, threadIdleStrategy);
    cacheProgressionThread = new CacheProgressionThread(cacheNameToCache.values(), threadIdleStrategy);

    return true;
  }

  /**
   * Start the server, requires {@link #init()} to be called first.
   */
  public void start() {
    responderThread.start();
    cacheProgressionThread.start();
    acceptorThread.start();
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
    acceptorThread.interrupt();
    cacheProgressionThread.interrupt();
    responderThread.interrupt();
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
