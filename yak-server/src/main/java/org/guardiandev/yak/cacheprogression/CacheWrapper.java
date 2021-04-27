package org.guardiandev.yak.cacheprogression;

import java.nio.ByteBuffer;
import org.agrona.concurrent.OneToOneConcurrentArrayQueue;
import org.guardiandev.yak.YakCache;
import org.guardiandev.yak.acceptor.IncomingCacheRequest;
import org.guardiandev.yak.pool.MemoryPool;
import org.guardiandev.yak.responder.CacheResponse;
import org.guardiandev.yak.responder.CacheResponseToResponderBridge;

/**
 * Cache wrapper that is capable of executing requests against its supplied cache.
 * <p>
 * the supplied cache may be null in the constructor, if so, all responses are cache not found.
 * </p>
 */
public final class CacheWrapper {

  private final YakCache<String, ByteBuffer> cache;
  private final CacheResponseToResponderBridge responderBridge;
  private final MemoryPool<IncomingCacheRequest> incomingCacheRequestPool;
  private final OneToOneConcurrentArrayQueue<IncomingCacheRequest> incomingCacheRequests;
  private final CacheResponse cacheResponse;

  /**
   * Initialise the cache wrapper for the given cache.
   *
   * @param cache                    the cache to execute requests against, if null all responses are cache not found
   * @param responderBridge          the responder to send responses to once executed
   * @param incomingCacheRequestPool the memory pool to return incoming cache requests to once processed
   */
  public CacheWrapper(final YakCache<String, ByteBuffer> cache,
                      final CacheResponseToResponderBridge responderBridge,
                      final MemoryPool<IncomingCacheRequest> incomingCacheRequestPool) {

    this.cache = cache;
    this.responderBridge = responderBridge;
    this.incomingCacheRequestPool = incomingCacheRequestPool;
    this.incomingCacheRequests = new OneToOneConcurrentArrayQueue<>(100);
    this.cacheResponse = new CacheResponse();
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
   */
  public void progressIncomingRequests() {
    incomingCacheRequests.drain(this::progressIncomingRequest);
  }

  private void progressIncomingRequest(final IncomingCacheRequest request) {

    cacheResponse.reset();

    // cache null means this is a responder for all non existing caches
    if (cache == null) {
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
      }
    }

    responderBridge.acceptCacheResponse(cacheResponse);
    incomingCacheRequestPool.returnToPool(request);
  }

  private void processGetRequest(final IncomingCacheRequest request) {
    final var result = cache.get(request.getKeyName());

    if (result == null) {
      cacheResponse.asKeyNotFound(request.getKeyName(), request.getResponder());
    } else {
      cacheResponse.asFound(request.getKeyName(), result, request.getResponder());
    }
  }

  private void processCreateRequest(final IncomingCacheRequest request) {
    cache.put(request.getKeyName(), request.getContent());
    cacheResponse.asCreated(request.getKeyName(), request.getResponder());
  }
}
