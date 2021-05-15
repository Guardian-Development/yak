package org.guardiandevelopment.yak.server.acceptor;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.UUID;
import org.guardiandevelopment.yak.server.cacheprogression.IncomingConnectionToCacheWrapperBridge;
import org.guardiandevelopment.yak.server.http.Constants;
import org.guardiandevelopment.yak.server.pool.MemoryPool;
import org.guardiandevelopment.yak.server.responder.HttpResponder;

public final class HttpRequestProcessor {

  private final IncomingConnectionToCacheWrapperBridge cacheWrapperBridge;
  private final MemoryPool<ByteBuffer> networkBufferPool;
  private final MemoryPool<IncomingCacheRequest> incomingCacheRequestMemoryPool;

  public HttpRequestProcessor(final IncomingConnectionToCacheWrapperBridge cacheWrapperBridge,
                              final MemoryPool<ByteBuffer> networkBufferPool,
                              final MemoryPool<IncomingCacheRequest> incomingCacheRequestMemoryPool) {
    this.cacheWrapperBridge = cacheWrapperBridge;
    this.networkBufferPool = networkBufferPool;
    this.incomingCacheRequestMemoryPool = incomingCacheRequestMemoryPool;
  }

  // TODO: test this and then move on to doing the health / cache endpoint config
  public void processReadyRequest(final HttpRequest request, final SocketChannel rawConnection) {
    final var uriParts = request.getRequestUri().split(Constants.SLASH);
    final var key = uriParts[uriParts.length - 1];
    final var cache = uriParts[uriParts.length - 2];
    final var type = typeFromMethod(request.getMethod());

    final var cacheRequest = incomingCacheRequestMemoryPool.take();
    cacheRequest.reset();

    var requestId = request.getHeaderOrNull(Constants.HTTP_REQUEST_ID_HEADER);
    requestId = requestId == null ? UUID.randomUUID().toString() : requestId;

    cacheRequest.setRequestId(requestId)
            .setResponder(new HttpResponder(rawConnection, networkBufferPool, requestId))
            .setCacheName(cache)
            .setKeyName(key)
            .setType(type)
            .setContent(request.getRequestBody());

    cacheWrapperBridge.acceptIncomingConnection(cacheRequest);
  }

  private IncomingCacheRequestType typeFromMethod(final String httpMethod) {
    if (Constants.HTTP_GET_METHOD.equalsIgnoreCase(httpMethod)) {
      return IncomingCacheRequestType.GET;
    }

    if (Constants.HTTP_POST_METHOD.equalsIgnoreCase(httpMethod)) {
      return IncomingCacheRequestType.CREATE;
    }

    return IncomingCacheRequestType.NOT_SUPPORTED;
  }
}
