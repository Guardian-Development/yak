package org.guardiandev.yak;

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
import java.nio.file.Path;

public final class YakServerRunner {

  private final YakServerConfig config;

  private ConnectionAcceptorThread acceptorThread;
  private CacheProgressionThread cacheProgressionThread;
  private ResponderThread responderThread;

  public YakServerRunner(final YakServerConfig config) {
    this.config = config;
  }

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

    responderThread = new ResponderThread();

    final var cacheResponseBridge = new CacheResponseToResponderBridge(responderThread);
    final var cacheInit = new CacheInitializer(config.getCaches());
    final var cacheNameToCache = cacheInit.init(cacheResponseBridge, incomingCacheRequestMemoryPool);

    final var connectionFactory = new IncomingConnectionFactory(networkBufferPool, httpRequestMemoryPool, incomingCacheRequestMemoryPool);
    final var connectionCacheBridge = new IncomingConnectionToCacheWrapperBridge(cacheNameToCache);

    acceptorThread = new ConnectionAcceptorThread(config.getPort(), connectionFactory, connectionCacheBridge);
    cacheProgressionThread = new CacheProgressionThread(cacheNameToCache.values());

    return true;
  }

  public void start() {
    responderThread.start();
    cacheProgressionThread.start();
    acceptorThread.start();
  }

  public boolean isRunning() {
    return acceptorThread.isAlive() && cacheProgressionThread.isAlive() && responderThread.isAlive();
  }

  public void stop() {
    acceptorThread.interrupt();
    cacheProgressionThread.interrupt();
    responderThread.interrupt();
  }

  public static void main(final String[] args) {
    final var configLocation = Path.of("/Users/Joe.Honour/code/yak/yak-server/src/test/resources/config/good-yak-config.json");
    final var config = new YakConfigFromJsonBuilder(configLocation).load();

    final var runner = new YakServerRunner(config);
    runner.init();
    runner.start();
  }
}
