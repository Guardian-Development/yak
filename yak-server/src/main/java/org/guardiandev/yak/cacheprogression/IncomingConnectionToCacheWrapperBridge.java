package org.guardiandev.yak.cacheprogression;

import org.guardiandev.yak.acceptor.IncomingCacheRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IncomingConnectionToCacheWrapperBridge {

  private static final Logger LOG = LoggerFactory.getLogger(IncomingConnectionToCacheWrapperBridge.class);

  // TODO: begin here, continue on the get journey, just get a nonsense value out of the cache and return
  //   goal is to do the get journey from postman end-to-end, then setup the first integration test
  public void acceptIncomingConnection(final IncomingCacheRequest request) {
    LOG.trace("accepted incoming cache request {}", request);
  }
}
