package org.guardiandev.yak.acceptor;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public final class IncomingCacheRequest {

  private IncomingCacheRequestType type;
  private String cacheName;
  private String keyName;
  private SocketChannel resultChannel;

  private final ByteBuffer content;

  public IncomingCacheRequest() {
    // TODO: memory pool
    this.content = ByteBuffer.allocate(512);
  }

  public IncomingCacheRequestType getType() {
    return type;
  }

  public IncomingCacheRequest setType(IncomingCacheRequestType type) {
    this.type = type;
    return this;
  }

  public String getCacheName() {
    return cacheName;
  }

  public IncomingCacheRequest setCacheName(String cacheName) {
    this.cacheName = cacheName;
    return this;
  }

  public String getKeyName() {
    return keyName;
  }

  public IncomingCacheRequest setKeyName(String keyName) {
    this.keyName = keyName;
    return this;
  }

  public SocketChannel getResultChannel() {
    return resultChannel;
  }

  public IncomingCacheRequest setResultChannel(SocketChannel resultChannel) {
    this.resultChannel = resultChannel;
    return this;
  }

  public ByteBuffer getContent() {
    return content;
  }

  public IncomingCacheRequest setContent(final ByteBuffer buffer) {
    content.put(buffer);
    content.flip();
    return this;
  }

  public void reset() {
    this.type = null;
    this.cacheName = null;
    this.keyName = null;
    this.resultChannel = null;
    this.content.clear();
  }
}
