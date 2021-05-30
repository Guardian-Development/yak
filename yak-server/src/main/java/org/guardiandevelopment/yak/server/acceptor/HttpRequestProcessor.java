package org.guardiandevelopment.yak.server.acceptor;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.guardiandevelopment.yak.server.cacheprogression.IncomingConnectionToCacheWrapperBridge;
import org.guardiandevelopment.yak.server.config.YakEndpointConfig;
import org.guardiandevelopment.yak.server.http.Constants;
import org.guardiandevelopment.yak.server.pool.MemoryPool;
import org.guardiandevelopment.yak.server.responder.HttpResponder;
import org.guardiandevelopment.yak.server.responder.ResponderBridge;

/**
 * Responsible for taking a http request, and routing it internally into the application based on its contents.
 */
public final class HttpRequestProcessor {

  private static final Pattern HTTP_ENDPOINT_PATTERN = Pattern.compile("(http://(.)+?)*/(?<resource>.+)");

  private final YakEndpointConfig endpointConfig;
  private final IncomingConnectionToCacheWrapperBridge cacheWrapperBridge;
  private final ResponderBridge responderBridge;
  private final MemoryPool<ByteBuffer> networkBufferPool;
  private final MemoryPool<IncomingCacheRequest> incomingCacheRequestMemoryPool;
  private final Matcher endpointMatcher;

  /**
   * Request processor for the given config. This should exist only once in the application for http request ingress.
   *
   * @param endpointConfig                 the configuration for where cache requests, or health check requests, should come from
   * @param cacheWrapperBridge             the place to send identified cache requests
   * @param responderBridge                the place to send immediate responses to (health checks, unsupported operations)
   * @param networkBufferPool              pool for buffers used in network connections
   * @param incomingCacheRequestMemoryPool cache request memory pool
   */
  public HttpRequestProcessor(final YakEndpointConfig endpointConfig,
                              final IncomingConnectionToCacheWrapperBridge cacheWrapperBridge,
                              final ResponderBridge responderBridge,
                              final MemoryPool<ByteBuffer> networkBufferPool,
                              final MemoryPool<IncomingCacheRequest> incomingCacheRequestMemoryPool) {
    this.endpointConfig = endpointConfig;
    this.cacheWrapperBridge = cacheWrapperBridge;
    this.responderBridge = responderBridge;
    this.networkBufferPool = networkBufferPool;
    this.incomingCacheRequestMemoryPool = incomingCacheRequestMemoryPool;
    this.endpointMatcher = HTTP_ENDPOINT_PATTERN.matcher("");
  }

  /**
   * Given a request, routes it to the health endpoint, or internally build a cache request.
   * <p>
   * if no route is found for the request, a 400 is returned.
   * </p>
   *
   * @param request       the incoming request
   * @param rawConnection the connection the response needs to be sent to
   */
  public void processReadyRequest(final HttpRequest request, final SocketChannel rawConnection) {
    endpointMatcher.reset(request.getRequestUri());

    if (endpointMatcher.matches()) {
      final var requestedResource = endpointMatcher.group("resource");

      if (requestedResource.equals(endpointConfig.getHealthCheck()) && "GET".equalsIgnoreCase(request.getMethod())) {
        handleHealthCheck(request, rawConnection);
      } else if (requestedResource.startsWith(endpointConfig.getCache())) {
        handleCacheRequest(request, rawConnection);
      } else {
        handleUnsupportedOperation(request, rawConnection);
      }

    } else {
      handleUnsupportedOperation(request, rawConnection);
    }
  }

  private void handleHealthCheck(final HttpRequest request, final SocketChannel rawConnection) {
    final var requestId = getRequestId(request);
    responderBridge.bufferHealthCheckResponse(new HttpResponder(rawConnection, networkBufferPool, requestId), true);
  }

  private void handleUnsupportedOperation(final HttpRequest request, final SocketChannel rawConnection) {
    final var requestId = getRequestId(request);
    responderBridge.bufferUnsupportedOperationResponse(new HttpResponder(rawConnection, networkBufferPool, requestId));
  }

  private void handleCacheRequest(final HttpRequest request, final SocketChannel rawConnection) {
    final var uriParts = request.getRequestUri().split(Constants.SLASH);
    final var key = uriParts[uriParts.length - 1];
    final var cache = uriParts[uriParts.length - 2];
    final var type = typeFromMethod(request.getMethod());

    final var cacheRequest = incomingCacheRequestMemoryPool.take();
    cacheRequest.reset();

    final var requestId = getRequestId(request);

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

  private String getRequestId(final HttpRequest request) {
    var requestId = request.getHeaderOrNull(Constants.HTTP_REQUEST_ID_HEADER);
    return requestId == null ? UUID.randomUUID().toString() : requestId;
  }
}
