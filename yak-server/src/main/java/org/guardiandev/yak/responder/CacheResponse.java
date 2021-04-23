package org.guardiandev.yak.responder;

import java.nio.ByteBuffer;

public final class CacheResponse {

  private CacheResponseType type;
  private String key;
  private ByteBuffer value;
  private Responder responder;

  public CacheResponseType getType() {
    return type;
  }

  public String getKey() {
    return key;
  }

  public ByteBuffer getValue() {
    return value;
  }

  public Responder getResponder() {
    return responder;
  }

  public CacheResponse asFound(final String key, final ByteBuffer value, final Responder responder) {
    this.key = key;
    this.value = value;
    this.responder = responder;

    this.type = CacheResponseType.FOUND;
    return this;
  }

  public CacheResponse asNotFound(final String key, final Responder responder) {
    this.key = key;
    this.responder = responder;

    this.type = CacheResponseType.NOT_FOUND;
    return this;
  }

  public CacheResponse asCreated(final String key, final Responder responder) {
    this.key = key;
    this.responder = responder;

    this.type = CacheResponseType.CREATED;
    return this;
  }

  public void clear() {
    this.type = null;
    this.key = null;
    this.value = null;
    this.responder = null;
  }
}
