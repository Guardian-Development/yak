package org.guardiandev.yak.cacheprogression;

import org.guardiandev.yak.acceptor.IncomingCacheRequest;

import java.util.Map;

/**
 * Provides routing for incoming connections, to the cache they need to execute over.
 */
public final class IncomingConnectionToCacheWrapperBridge {

  private final Map<String, CacheWrapper> cacheNameToWrapper;

  // todo: possibly have null/error cache for when caches not found
  public IncomingConnectionToCacheWrapperBridge(final Map<String, CacheWrapper> cacheNameToWrapper) {
    this.cacheNameToWrapper = cacheNameToWrapper;
  }

  /**
   * Routes the request to the correct cache.
   * <p>
   * the caller of this method should not modify the request object after this method returns, as the bridge
   * buffers requests for downstream threads to pickup at their leisure. Therefore, all buffers and memory
   * entering the bridge must be seen as used until explicitly released by a downstream thread.
   * </p>
   *
   * @param request the request to route
   */
  public void acceptIncomingConnection(final IncomingCacheRequest request) {
    final var name = request.getCacheName();
    final var cache = cacheNameToWrapper.get(name);
    cache.bufferRequest(request);
  }
}
