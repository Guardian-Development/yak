package org.guardiandev.yak.acceptor;

import java.nio.channels.ByteChannel;
import java.nio.channels.SocketChannel;

public final class IncomingCacheRequest {
  private final String cacheName;
  private final String keyName;
  private final ByteChannel resultChannel;

  public IncomingCacheRequest(final String cacheName, final String keyName, final SocketChannel resultChannel) {
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

  public ByteChannel getResultChannel() {
    return resultChannel;
  }
}
