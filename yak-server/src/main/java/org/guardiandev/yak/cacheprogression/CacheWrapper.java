package org.guardiandev.yak.cacheprogression;

import org.agrona.concurrent.OneToOneConcurrentArrayQueue;
import org.guardiandev.yak.YakCache;
import org.guardiandev.yak.acceptor.IncomingCacheRequest;
import org.guardiandev.yak.responder.CacheResponse;
import org.guardiandev.yak.responder.CacheResponseToResponderBridge;

import java.nio.ByteBuffer;

public final class CacheWrapper {

  private final YakCache<String, ByteBuffer> cache;
  private final CacheResponseToResponderBridge responderBridge;
  private final OneToOneConcurrentArrayQueue<IncomingCacheRequest> incomingCacheRequests;
  private final CacheResponse cacheResponse;

  public CacheWrapper(final YakCache<String, ByteBuffer> cache, final CacheResponseToResponderBridge responderBridge) {

    this.cache = cache;
    this.responderBridge = responderBridge;
    this.incomingCacheRequests = new OneToOneConcurrentArrayQueue<>(100);
    this.cacheResponse = new CacheResponse();
  }

  public boolean bufferRequest(final IncomingCacheRequest request) {

    return incomingCacheRequests.offer(request);
  }

  public void progressIncomingRequests() {
    incomingCacheRequests.drain(this::progressIncomingRequest);
  }

  private void progressIncomingRequest(final IncomingCacheRequest request) {

    cacheResponse.clear();

    switch (request.getType()) {
      case GET:
        processGetRequest(request);
        break;
      case CREATE:
        processCreateRequest(request);
        break;
    }

    responderBridge.acceptCacheResponse(cacheResponse);
  }

  private void processGetRequest(final IncomingCacheRequest request) {
    final var result = cache.get(request.getKeyName());

    if (result == null) {
      cacheResponse.asNotFound(request.getKeyName(), request.getResultChannel());
    } else {
      cacheResponse.asFound(request.getKeyName(), result, request.getResultChannel());
    }
  }

  private void processCreateRequest(final IncomingCacheRequest request) {
    cache.put(request.getKeyName(), request.getContent());
    cacheResponse.asCreated(request.getKeyName(), request.getResultChannel());
  }
}
