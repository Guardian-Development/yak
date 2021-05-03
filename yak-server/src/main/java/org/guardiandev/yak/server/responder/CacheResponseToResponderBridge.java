package org.guardiandev.yak.server.responder;

/**
 * Provides routing between the cache responses executed, and the responder thread.
 */
public final class CacheResponseToResponderBridge {

  private final ResponderThread responderThread;

  /**
   * Creates the bridge.
   *
   * @param responderThread the responder thread to route responses to
   */
  public CacheResponseToResponderBridge(final ResponderThread responderThread) {

    this.responderThread = responderThread;
  }

  /**
   * Builds the response and buffer it on the responder.
   *
   * @param response the response from executing the cache request
   */
  public void acceptCacheResponse(final CacheResponse response) {

    final var responder = response.getResponder();
    switch (response.getType()) {
      case FOUND:
        responder.bufferResponse(Result.KEY_FOUND, response.getValue());
        break;
      case KEY_NOT_FOUND:
        responder.bufferResponse(Result.KEY_NOT_FOUND, null);
        break;
      case CACHE_NOT_FOUND:
        responder.bufferResponse(Result.CACHE_NOT_FOUND, null);
        break;
      case CREATED:
        responder.bufferResponse(Result.KEY_CREATED, null);
        break;
      default:
        responder.bufferResponse(Result.INTERNAL_ERROR, null);
    }

    responderThread.bufferResponse(responder);
  }
}
