package org.guardiandev.yak.acceptor;

import java.nio.channels.SocketChannel;

public final class IncomingCacheRequest {

  private final IncomingCacheRequestType type;
  private final String cacheName;
  private final String keyName;
  private final SocketChannel resultChannel;

  public IncomingCacheRequest(final IncomingCacheRequestType type,
                              final String cacheName,
                              final String keyName,
                              final SocketChannel resultChannel) {
    this.type = type;
    this.cacheName = cacheName;
    this.keyName = keyName;
    this.resultChannel = resultChannel;
  }

  public String getCacheName() {
    return cacheName;
  }

  public String getKeyName() {
    return keyName;
  }

  public SocketChannel getResultChannel() {
    return resultChannel;
  }
}
