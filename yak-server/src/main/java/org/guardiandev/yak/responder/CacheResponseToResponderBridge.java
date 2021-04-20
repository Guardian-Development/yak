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
    LOG.trace("accepted response of type {}", response);

    final var responder = new HttpResponder(response.getResultChannel());
    if (response.getType() == CacheResponseType.NOT_FOUND) {
      responder.bufferResponse(Result.KEY_NOT_FOUND, null);
    }

    responderThread.bufferResponse(responder);
  }
}
