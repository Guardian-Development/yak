package org.guardiandev.yak.server.cacheprogression;

import java.util.Map;
import org.guardiandev.yak.server.acceptor.IncomingCacheRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides routing for incoming connections, to the cache they need to execute over.
 */
public final class IncomingConnectionToCacheWrapperBridge {

  private static final Logger LOG = LoggerFactory.getLogger(IncomingConnectionToCacheWrapperBridge.class);

  private final Map<String, CacheWrapper> cacheNameToWrapper;

  /**
   * Creates the bridge.
   *
   * @param cacheNameToWrapper the available cache wrappers to route requests to
   */
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

    var cache = cacheNameToWrapper.get(name);

    if (cache == null) {
      LOG.debug("[requestId={},cacheName={}] no cache found for request", request.getRequestId(), name);
      cache = cacheNameToWrapper.get(CacheInitializer.NULL_CACHE_RESPONDER_KEY);
    }

    LOG.debug("[requestId={},cacheName={}] buffering request with cache wrapper", request.getRequestId(), name);
    cache.bufferRequest(request);
  }
}
