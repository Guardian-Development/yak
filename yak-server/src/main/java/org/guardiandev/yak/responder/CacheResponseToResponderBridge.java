package org.guardiandev.yak.responder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: picks correct responder (i.e. found/not-found, http/custom) then registers with responder thread
public final class CacheResponseToResponderBridge {

  private static final Logger LOG = LoggerFactory.getLogger(CacheResponseToResponderBridge.class);

  private final ResponderThread responderThread;

  public CacheResponseToResponderBridge(final ResponderThread responderThread) {

    this.responderThread = responderThread;
  }

  // TODO: use a factory for the responder from the original request
  public void acceptCacheResponse(final CacheResponse response) {

    final var responder = new HttpResponder(response.getResultChannel());
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
