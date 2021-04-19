package org.guardiandev.yak.cacheprogression;

import org.guardiandev.yak.acceptor.IncomingCacheRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class IncomingConnectionToCacheWrapperBridge {

  private static final Logger LOG = LoggerFactory.getLogger(IncomingConnectionToCacheWrapperBridge.class);
  private final Map<String, CacheWrapper> cacheNameToWrapper;

  public IncomingConnectionToCacheWrapperBridge(final Map<String, CacheWrapper> cacheNameToWrapper) {
    this.cacheNameToWrapper = cacheNameToWrapper;
  }

  public void acceptIncomingConnection(final IncomingCacheRequest request) {
    final var name = request.getCacheName();
    final var cache = cacheNameToWrapper.get(name);
    cache.bufferRequest(request);
  }
}
