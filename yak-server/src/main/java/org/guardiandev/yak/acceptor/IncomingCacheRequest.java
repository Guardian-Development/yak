package org.guardiandev.yak.acceptor;

import org.guardiandev.yak.responder.Responder;

import java.nio.ByteBuffer;

public final class IncomingCacheRequest {

  private IncomingCacheRequestType type;
  private String cacheName;
  private String keyName;
  private Responder responder;

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

  public ByteBuffer getContent() {
    return content;
  }

  public IncomingCacheRequest setContent(final ByteBuffer buffer) {
    content.put(buffer);
    content.flip();
    return this;
  }

  public Responder getResponder() {
    return responder;
  }

  public IncomingCacheRequest setResponder(Responder responder) {
    this.responder = responder;
    return this;
  }

  public void reset() {
    this.type = null;
    this.cacheName = null;
    this.keyName = null;
    this.responder = null;
    this.content.clear();
  }
}
