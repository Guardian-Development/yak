package org.guardiandev.yak.cacheprogression;

import org.guardiandev.yak.acceptor.IncomingCacheRequest;

import java.util.Map;

public final class IncomingConnectionToCacheWrapperBridge {

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
