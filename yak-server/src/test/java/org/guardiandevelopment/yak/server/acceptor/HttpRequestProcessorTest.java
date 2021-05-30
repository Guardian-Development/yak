package org.guardiandevelopment.yak.server.acceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.guardiandevelopment.yak.server.cacheprogression.IncomingConnectionToCacheWrapperBridge;
import org.guardiandevelopment.yak.server.config.YakEndpointConfig;
import org.guardiandevelopment.yak.server.pool.Factory;
import org.guardiandevelopment.yak.server.pool.MemoryPool;
import org.guardiandevelopment.yak.server.responder.ResponderBridge;
import org.guardiandevelopment.yak.server.testsupport.ByteBufferAssertion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HttpRequestProcessorTest {

  private final int bufferSize = 256;
  private final MemoryPool<ByteBuffer> networkBufferPool = Factory.networkBufferPool(2, bufferSize, true);
  private final MemoryPool<IncomingCacheRequest> incomingCacheRequestPool = Factory.incomingCacheRequestPool(2, bufferSize, true);
  private final YakEndpointConfig config = new YakEndpointConfig().setCache("cache").setHealthCheck("health");

  @Test
  void shouldTransformGetRequestToGetCacheRequest(@Mock IncomingConnectionToCacheWrapperBridge bridge,
                                                  @Mock ResponderBridge responderBridge,
                                                  @Mock SocketChannel channel) {
    // Arrange
    final var unitUnderTest = new HttpRequestProcessor(config, bridge, responderBridge, networkBufferPool, incomingCacheRequestPool);
    final var requestInput = new HttpRequest(0)
            .setRequestUri("http://localhost:8080/cache/root/test")
            .setMethod("GET")
            .addHeader("x-request-id", "test");

    // Act
    unitUnderTest.processReadyRequest(requestInput, channel);

    // Assert
    final var requestCapture = ArgumentCaptor.forClass(IncomingCacheRequest.class);
    verify(bridge).acceptIncomingConnection(requestCapture.capture());
    final var request = requestCapture.getValue();

    assertThat(request.getType()).isEqualTo(IncomingCacheRequestType.GET);
    assertThat(request.getCacheName()).isEqualTo("root");
    assertThat(request.getKeyName()).isEqualTo("test");
    assertThat(request.getRequestId()).isEqualTo("test");
  }

  @Test
  void shouldTransformPostRequestToCreateCacheRequest(@Mock IncomingConnectionToCacheWrapperBridge bridge,
                                                      @Mock ResponderBridge responderBridge,
                                                      @Mock SocketChannel channel) {
    // Arrange
    final var unitUnderTest = new HttpRequestProcessor(config, bridge, responderBridge, networkBufferPool, incomingCacheRequestPool);
    final var requestInput = new HttpRequest(256)
            .setRequestUri("http://localhost:8080/cache/root/test")
            .setMethod("POST")
            .addHeader("x-request-id", "test")
            .addHeader("content-length", "5")
            .copyIntoRequestBody(ByteBuffer.wrap("12345".getBytes()));

    // Act
    unitUnderTest.processReadyRequest(requestInput, channel);

    // Assert
    final var requestCapture = ArgumentCaptor.forClass(IncomingCacheRequest.class);
    verify(bridge).acceptIncomingConnection(requestCapture.capture());
    final var request = requestCapture.getValue();
    assertThat(request.getType()).isEqualTo(IncomingCacheRequestType.CREATE);
    assertThat(request.getCacheName()).isEqualTo("root");
    assertThat(request.getKeyName()).isEqualTo("test");
    assertThat(request.getRequestId()).isEqualTo("test");

    ByteBufferAssertion.assertThat(request.getContent()).isEqualTo("12345");
  }

  @Test
  void shouldIdentifyHealthCheckRequestAndSendResponse(@Mock IncomingConnectionToCacheWrapperBridge bridge,
                                                       @Mock ResponderBridge responderBridge,
                                                       @Mock SocketChannel channel) {
    // Arrange
    final var unitUnderTest = new HttpRequestProcessor(config, bridge, responderBridge, networkBufferPool, incomingCacheRequestPool);
    final var requestInput = new HttpRequest(256)
            .setRequestUri("http://localhost:8080/health")
            .setMethod("GET")
            .addHeader("x-request-id", "test")
            .addHeader("content-length", "5")
            .copyIntoRequestBody(ByteBuffer.wrap("12345".getBytes()));

    // Act
    unitUnderTest.processReadyRequest(requestInput, channel);

    // Assert
    verify(responderBridge).bufferHealthCheckResponse(any(), eq(true));
  }

  @Test
  void shouldOnlySupportGetOnHealthCheckRequest(@Mock IncomingConnectionToCacheWrapperBridge bridge,
                                                @Mock ResponderBridge responderBridge,
                                                @Mock SocketChannel channel) {
    // Arrange
    final var unitUnderTest = new HttpRequestProcessor(config, bridge, responderBridge, networkBufferPool, incomingCacheRequestPool);
    final var requestInput = new HttpRequest(256)
            .setRequestUri("http://localhost:8080/health")
            .setMethod("POST")
            .addHeader("x-request-id", "test")
            .addHeader("content-length", "5")
            .copyIntoRequestBody(ByteBuffer.wrap("12345".getBytes()));

    // Act
    unitUnderTest.processReadyRequest(requestInput, channel);

    // Assert
    verify(responderBridge).bufferUnsupportedOperationResponse(any());
  }

  @Test
  void shouldIdentifyUnsupportedOperationIfRequestIsNotMatchingRootCacheResource(@Mock IncomingConnectionToCacheWrapperBridge bridge,
                                                                                 @Mock ResponderBridge responderBridge,
                                                                                 @Mock SocketChannel channel) {
    // Arrange
    final var unitUnderTest = new HttpRequestProcessor(config, bridge, responderBridge, networkBufferPool, incomingCacheRequestPool);
    final var requestInput = new HttpRequest(256)
            .setRequestUri("http://localhost:8080/not-root/cache/test")
            .setMethod("POST")
            .addHeader("x-request-id", "test")
            .addHeader("content-length", "5")
            .copyIntoRequestBody(ByteBuffer.wrap("12345".getBytes()));

    // Act
    unitUnderTest.processReadyRequest(requestInput, channel);

    // Assert
    verify(responderBridge).bufferUnsupportedOperationResponse(any());
  }

  @Test
  void shouldIdentifyUnsupportedOperationIfUrlDoesNotConformToRequestPattern(@Mock IncomingConnectionToCacheWrapperBridge bridge,
                                                                             @Mock ResponderBridge responderBridge,
                                                                             @Mock SocketChannel channel) {
    // Arrange
    final var unitUnderTest = new HttpRequestProcessor(config, bridge, responderBridge, networkBufferPool, incomingCacheRequestPool);
    final var requestInput = new HttpRequest(256)
            .setRequestUri("http://localhost:8080")
            .setMethod("POST")
            .addHeader("x-request-id", "test")
            .addHeader("content-length", "5")
            .copyIntoRequestBody(ByteBuffer.wrap("12345".getBytes()));

    // Act
    unitUnderTest.processReadyRequest(requestInput, channel);

    // Assert
    verify(responderBridge).bufferUnsupportedOperationResponse(any());
  }
}
