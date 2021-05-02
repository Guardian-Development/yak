package org.guardiandev.yak.acceptor;

import java.nio.ByteBuffer;
import org.guardiandev.yak.responder.Responder;

/**
 * Represents an incoming cache request, with all the information needed to execute and respond to the request.
 */
public final class IncomingCacheRequest {

  private IncomingCacheRequestType type;
  private String cacheName;
  private String keyName;
  private Responder responder;

  private final ByteBuffer content;

  public IncomingCacheRequest(final int bufferSize) {
    this.content = ByteBuffer.allocateDirect(bufferSize);
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

  /**
   * Copies the buffer into the {@link #content} buffer, using its current position and limit.
   * <p>
   * the buffers position is not reset after writing, so it will be left with a position equal to limit after.
   * </p>
   *
   * @param buffer the content to copy
   * @return self
   */
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

  /**
   * Reset the object ready for it to be used again in a pool.
   *
   * @return the same object ready for reuse
   */
  public IncomingCacheRequest reset() {
    this.type = null;
    this.cacheName = null;
    this.keyName = null;
    this.responder = null;
    this.content.clear();

    return this;
  }
}
