package org.guardiandevelopment.yak.server.cacheprogression;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import org.agrona.concurrent.OneToOneConcurrentArrayQueue;
import org.guardiandevelopment.yak.core.YakCache;
import org.guardiandevelopment.yak.server.acceptor.IncomingCacheRequest;
import org.guardiandevelopment.yak.server.pool.MemoryPool;
import org.guardiandevelopment.yak.server.responder.CacheResponse;
import org.guardiandevelopment.yak.server.responder.CacheResponseToResponderBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache wrapper that is capable of executing requests against its supplied cache.
 * <p>
 * the supplied cache may be null in the constructor, if so, all responses are cache not found.
 * </p>
 */
public final class CacheWrapper {

  private static final Logger LOG = LoggerFactory.getLogger(CacheWrapper.class);

  private final String cacheName;
  private final YakCache<String, ByteBuffer> cache;
  private final CacheResponseToResponderBridge responderBridge;
  private final MemoryPool<IncomingCacheRequest> incomingCacheRequestPool;
  private final OneToOneConcurrentArrayQueue<IncomingCacheRequest> incomingCacheRequests;
  private final CacheRequestExecutor cacheRequestExecutor;

  /**
   * Initialise the cache wrapper for the given cache.
   *
   * @param cacheName                the name of this cache, used for logging purposes
   * @param cache                    the cache to execute requests against, if null all responses are cache not found
   * @param responderBridge          the responder to send responses to once executed
   * @param incomingCacheRequestPool the memory pool to return incoming cache requests to once processed
   */
  public CacheWrapper(final String cacheName,
                      final YakCache<String, ByteBuffer> cache,
                      final CacheResponseToResponderBridge responderBridge,
                      final MemoryPool<IncomingCacheRequest> incomingCacheRequestPool) {
    this.cacheName = cacheName;

    this.cache = cache;
    this.responderBridge = responderBridge;
    this.incomingCacheRequestPool = incomingCacheRequestPool;
    this.incomingCacheRequests = new OneToOneConcurrentArrayQueue<>(100);
    this.cacheRequestExecutor = new CacheRequestExecutor();
  }

  /**
   * Buffers the incoming cache request, which will be drained and picked up on the next execution of the wrapper.
   *
   * @param request the request to buffer
   * @return true if buffered successfully, else false
   */
  public boolean bufferRequest(final IncomingCacheRequest request) {

    return incomingCacheRequests.offer(request);
  }

  /**
   * Progress all waiting requests, this essentially ticks the wrapper.
   *
   * @return the number of requests progressed
   */
  public int progressIncomingRequests() {

    return incomingCacheRequests.drain(cacheRequestExecutor);
  }

  private final class CacheRequestExecutor implements Consumer<IncomingCacheRequest> {

    private final CacheResponse cacheResponse;

    CacheRequestExecutor() {
      this.cacheResponse = new CacheResponse();
    }

    @Override
    public void accept(final IncomingCacheRequest request) {

      LOG.debug("[cacheName={},key={},requestId={}] executing cache request", cacheName, request.getKeyName(), request.getRequestId());
      cacheResponse.reset();

      // cache null means this is a responder for all non existing caches
      if (cache == null) {
        LOG.debug("[cacheName={},key={},requestId={}] marking response as cache not found", cacheName, request.getKeyName(), request.getRequestId());
        cacheResponse.asCacheNotFound(request.getResponder());
      } else {

        switch (request.getType()) {
          case GET:
            processGetRequest(request);
            break;
          case CREATE:
            processCreateRequest(request);
            break;
          default:
            LOG.warn("[cacheName={},key={},requestId={},request={}] could not execute request", cacheName, request.getKeyName(), request.getRequestId(), request);
        }
      }

      responderBridge.acceptCacheResponse(cacheResponse);
      incomingCacheRequestPool.returnToPool(request);
    }

    private void processGetRequest(final IncomingCacheRequest request) {

      final var result = cache.get(request.getKeyName());

      if (result == null) {
        LOG.debug("[cacheName={},key={},requestId={}] key not found in cache", cacheName, request.getKeyName(), request.getRequestId());
        cacheResponse.asKeyNotFound(request.getKeyName(), request.getResponder());
      } else {
        LOG.debug("[cacheName={},key={},requestId={}] key found in cache", cacheName, request.getKeyName(), request.getRequestId());
        cacheResponse.asFound(request.getKeyName(), result, request.getResponder());
      }
    }

    private void processCreateRequest(final IncomingCacheRequest request) {

      cache.put(request.getKeyName(), request.getContent());

      LOG.debug("[cacheName={},key={},requestId={}] created key in cache", cacheName, request.getKeyName(), request.getRequestId());
      cacheResponse.asCreated(request.getKeyName(), request.getResponder());
    }
  }
}
