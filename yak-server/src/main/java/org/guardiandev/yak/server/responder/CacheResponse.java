package org.guardiandev.yak.server.responder;

import java.nio.ByteBuffer;

/**
 * Represents the outcome of executing a cache request from a {@link org.guardiandev.yak.server.cacheprogression.CacheWrapper}.
 */
public final class CacheResponse {

  private CacheResponseType type;
  private String key;
  private ByteBuffer value;
  private Responder responder;

  public CacheResponseType getType() {
    return type;
  }

  public ByteBuffer getValue() {
    return value;
  }

  public Responder getResponder() {
    return responder;
  }

  /**
   * Set a found response within the object.
   *
   * @param key       the key found
   * @param value     the value found
   * @param responder the responder to use when sending the response to the client
   * @return self
   */
  public CacheResponse asFound(final String key, final ByteBuffer value, final Responder responder) {
    this.key = key;
    this.value = value;
    this.responder = responder;

    this.type = CacheResponseType.FOUND;
    return this;
  }

  /**
   * Set a key not found response within the object.
   *
   * @param key       the key not found
   * @param responder the responder to use when sending the response to the client
   * @return self
   */
  public CacheResponse asKeyNotFound(final String key, final Responder responder) {
    this.key = key;
    this.responder = responder;

    this.type = CacheResponseType.KEY_NOT_FOUND;
    return this;
  }

  /**
   * Set a cache not found response within the object.
   *
   * @param responder the responder to use when sending the response to the client
   * @return self
   */
  public CacheResponse asCacheNotFound(final Responder responder) {
    this.responder = responder;

    this.type = CacheResponseType.CACHE_NOT_FOUND;
    return this;
  }

  /**
   * Set a created response within the object.
   *
   * @param key       the key created
   * @param responder the responder to use when sending the response to the client
   * @return self
   */
  public CacheResponse asCreated(final String key, final Responder responder) {
    this.key = key;
    this.responder = responder;

    this.type = CacheResponseType.CREATED;
    return this;
  }

  /**
   * Reset the object ready for it to be used again in a pool.
   *
   * @return the same object ready for reuse
   */
  public CacheResponse reset() {
    this.type = null;
    this.key = null;
    this.value = null;
    this.responder = null;

    return this;
  }
}
