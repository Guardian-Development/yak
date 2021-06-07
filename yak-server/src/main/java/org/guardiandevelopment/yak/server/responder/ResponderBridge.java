package org.guardiandevelopment.yak.server.responder;

/**
 * Provides routing between the cache responses executed, and the responder thread.
 */
public class ResponderBridge {

  private final ResponderThread responderThread;

  /**
   * Creates the bridge.
   *
   * @param responderThread the responder thread to route responses to
   */
  public ResponderBridge(final ResponderThread responderThread) {

    this.responderThread = responderThread;
  }

  /**
   * Builds the response and buffer it on the responder.
   *
   * @param response  the response from executing the cache request
   */
  public void bufferCacheResponse(final CacheResponse response) {

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

  /**
   * Builds the health check response and buffer it on the responder.
   *
   * @param responder the responder to send the response via
   * @param isHealthy true if the application is healthy, else false
   */
  public void bufferHealthCheckResponse(final Responder responder, final boolean isHealthy) {

    if (isHealthy) {
      responder.bufferResponse(Result.HEALTHY, null);
    } else {
      responder.bufferResponse(Result.NOT_HEALTHY, null);
    }

    responderThread.bufferResponse(responder);
  }

  /**
   * Builds the unsupported operation response and buffer it on the responder.
   *
   * @param responder the responder to send the response via
   */
  public void bufferUnsupportedOperationResponse(final Responder responder) {

    responder.bufferResponse(Result.NOT_SUPPORTED_OPERATION, null);

    responderThread.bufferResponse(responder);
  }
}
