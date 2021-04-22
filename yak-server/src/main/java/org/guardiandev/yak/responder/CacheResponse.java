package org.guardiandev.yak.responder;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public final class CacheResponse {

  private CacheResponseType type;
  private String key;
  private ByteBuffer value;
  private SocketChannel resultChannel;

  public CacheResponseType getType() {
    return type;
  }

  public String getKey() {
    return key;
  }

  public ByteBuffer getValue() {
    return value;
  }

  public SocketChannel getResultChannel() {
    return resultChannel;
  }

  public CacheResponse asFound(final String key, final ByteBuffer value, final SocketChannel resultChannel) {
    this.key = key;
    this.value = value;
    this.resultChannel = resultChannel;

    this.type = CacheResponseType.FOUND;
    return this;
  }

  public CacheResponse asNotFound(final String key, final SocketChannel resultChannel) {
    this.key = key;
    this.resultChannel = resultChannel;

    this.type = CacheResponseType.NOT_FOUND;
    return this;
  }

  public CacheResponse asCreated(final String key, final SocketChannel resultChannel) {
    this.key = key;
    this.resultChannel = resultChannel;

    this.type = CacheResponseType.CREATED;
    return this;
  }

  public void clear() {
    this.type = null;
    this.key = null;
    this.value = null;
    this.resultChannel = null;
  }
}
