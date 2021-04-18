package org.guardiandev.yak;

import org.guardiandev.yak.acceptor.ConnectionAcceptorThread;
import org.guardiandev.yak.acceptor.IncomingConnectionFactory;
import org.guardiandev.yak.cacheprogression.IncomingConnectionToCacheWrapperBridge;
import org.guardiandev.yak.config.YakConfigFromJsonBuilder;

import java.nio.file.Path;

public final class YakServerRunner {

  private final String configLocation;

  private ConnectionAcceptorThread acceptorThread;

  YakServerRunner(final String configLocation) {
    this.configLocation = configLocation;
  }

  public boolean init() {
    final var config = new YakConfigFromJsonBuilder(Path.of(configLocation)).load();

    final var connectionFactory = new IncomingConnectionFactory();
    final var connectionCacheBridge = new IncomingConnectionToCacheWrapperBridge();
    acceptorThread = new ConnectionAcceptorThread(config.getPort(), connectionFactory, connectionCacheBridge);

    return true;
  }

  public void run() {
    acceptorThread.start();
  }

  public static void main(final String[] args) {
    final var runner = new YakServerRunner("/Users/Joe.Honour/code/yak/yak-server/src/test/resources/config/good-yak-config.json");
    runner.init();
    runner.run();
  }
}
