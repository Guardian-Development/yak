package org.guardiandev.yak.responder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: picks correct responder (i.e. found/not-found, http/custom) then registers with responder thread
public final class CacheResponseToResponderBridge {

  private static Logger LOG = LoggerFactory.getLogger(CacheResponseToResponderBridge.class);

  // TODO: here in the chain now
  public void acceptCacheResponse(final CacheResponse response) {
    LOG.trace("accepted response of type {}", response);
  }
}
