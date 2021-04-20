package org.guardiandev.yak;

import org.guardiandev.yak.acceptor.ConnectionAcceptorThread;
import org.guardiandev.yak.acceptor.IncomingConnectionFactory;
import org.guardiandev.yak.cacheprogression.CacheInitializer;
import org.guardiandev.yak.cacheprogression.CacheProgressionThread;
import org.guardiandev.yak.cacheprogression.IncomingConnectionToCacheWrapperBridge;
import org.guardiandev.yak.config.YakConfigFromJsonBuilder;
import org.guardiandev.yak.responder.CacheResponseToResponderBridge;
import org.guardiandev.yak.responder.ResponderThread;

import java.nio.file.Path;

public final class YakServerRunner {

  private final String configLocation;

  private ConnectionAcceptorThread acceptorThread;
  private CacheProgressionThread cacheProgressionThread;
  private ResponderThread responderThread;

  YakServerRunner(final String configLocation) {
    this.configLocation = configLocation;
  }

  public boolean init() {
    final var config = new YakConfigFromJsonBuilder(Path.of(configLocation)).load();

    responderThread = new ResponderThread();

    final var cacheResponseBridge = new CacheResponseToResponderBridge(responderThread);
    final var cacheInit = new CacheInitializer(config.getCaches());
    final var cacheNameToCache = cacheInit.init(cacheResponseBridge);

    final var connectionFactory = new IncomingConnectionFactory();
    final var connectionCacheBridge = new IncomingConnectionToCacheWrapperBridge(cacheNameToCache);

    acceptorThread = new ConnectionAcceptorThread(config.getPort(), connectionFactory, connectionCacheBridge);
    cacheProgressionThread = new CacheProgressionThread(cacheNameToCache.values());

    return true;
  }

  public void run() {
    responderThread.start();
    cacheProgressionThread.start();
    acceptorThread.start();
  }

  public static void main(final String[] args) {
    final var runner = new YakServerRunner("/Users/Joe.Honour/code/yak/yak-server/src/test/resources/config/good-yak-config.json");
    runner.init();
    runner.run();
  }
}
