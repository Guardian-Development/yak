package org.guardiandev.yak.responder;

public final class CacheResponseToResponderBridge {

  private final ResponderThread responderThread;

  public CacheResponseToResponderBridge(final ResponderThread responderThread) {

    this.responderThread = responderThread;
  }

  public void acceptCacheResponse(final CacheResponse response) {

    final var responder = response.getResponder();
    switch (response.getType()) {
      case FOUND:
        responder.bufferResponse(Result.KEY_FOUND, response.getValue());
        break;
      case NOT_FOUND:
        responder.bufferResponse(Result.KEY_NOT_FOUND, null);
        break;
      case CREATED:
        responder.bufferResponse(Result.KEY_CREATED, null);
        break;
    }

    responderThread.bufferResponse(responder);
  }
}
