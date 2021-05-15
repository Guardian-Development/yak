package org.guardiandevelopment.yak.server.acceptor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.guardiandevelopment.yak.server.pool.Factory;
import org.guardiandevelopment.yak.server.pool.MemoryPool;
import org.guardiandevelopment.yak.server.testsupport.ByteBufferAssertion;
import org.guardiandevelopment.yak.server.testsupport.ByteLimitResponder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IncomingHttpConnectionTest {

  private final int bufferSize = 256;
  private final MemoryPool<ByteBuffer> networkBufferPool = Factory.networkBufferPool(2, bufferSize, true);
  private final MemoryPool<HttpRequest> httpRequestPool = Factory.httpRequestPool(2, bufferSize, true);

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 5, 8, 13, 50})
  void shouldExtractHttpRequestLineFromRequest(int limit, @Mock SocketChannel channel) throws IOException {
    // Arrange
    final var unitUnderTest = new IncomingHttpConnection(channel, networkBufferPool, httpRequestPool, "test");
    final var requestInput = "GET /cache/test HTTP/1.1 \r\n\r\n";
    ByteLimitResponder.returnBytesFromMock(channel, requestInput, limit);

    for (var i = 0; i < requestInput.length(); i++) {
      unitUnderTest.progress();
    }

    // Assert
    unitUnderTest.cleanup();

    final var request = unitUnderTest.getRequest();
    assertThat(request).usingRecursiveComparison()
            .ignoringFields("requestBody")
            .isEqualTo(new HttpRequest(bufferSize)
                    .setMethod("GET")
                    .setRequestUri("/cache/test")
                    .setHttpVersion("HTTP/1.1"));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 5, 8, 13, 50})
  void shouldExtractHttpRequestHeaderFromRequest(int limit, @Mock SocketChannel channel) throws IOException {
    // Arrange
    final var unitUnderTest = new IncomingHttpConnection(channel, networkBufferPool, httpRequestPool, "test");
    final var requestInput = "GET /cache/test HTTP/1.1 \r\nX-Request-Id: 12345z\r\n\r\n";
    ByteLimitResponder.returnBytesFromMock(channel, requestInput, limit);

    for (var i = 0; i < requestInput.length(); i++) {
      unitUnderTest.progress();
    }

    // Assert
    unitUnderTest.cleanup();

    final var request = unitUnderTest.getRequest();
    assertThat(request).usingRecursiveComparison()
            .ignoringFields("requestBody")
            .isEqualTo(new HttpRequest(bufferSize)
                    .setMethod("GET")
                    .setRequestUri("/cache/test")
                    .setHttpVersion("HTTP/1.1")
                    .addHeader("x-request-id", "12345z"));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 5, 8, 13, 50})
  void shouldExtractMultipleHeadersFromRequest(int limit, @Mock SocketChannel channel) throws IOException {
    // Arrange
    final var unitUnderTest = new IncomingHttpConnection(channel, networkBufferPool, httpRequestPool, "test");
    final var requestInput = "GET /cache/test HTTP/1.1 \r\nX-Request-Id: 12345z\r\nhost: localhost:1234\r\n\r\n";
    ByteLimitResponder.returnBytesFromMock(channel, requestInput, limit);

    for (var i = 0; i < requestInput.length(); i++) {
      unitUnderTest.progress();
    }

    // Assert
    final var request = unitUnderTest.getRequest();
    assertThat(request).usingRecursiveComparison()
            .ignoringFields("requestBody")
            .isEqualTo(new HttpRequest(bufferSize)
                    .setMethod("GET")
                    .setRequestUri("/cache/test")
                    .setHttpVersion("HTTP/1.1")
                    .addHeader("x-request-id", "12345z")
                    .addHeader("host", "localhost:1234"));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 5, 8, 13, 50})
  void shouldExtractBodyUsingContentLengthHeaderSize(int limit, @Mock SocketChannel channel) throws IOException {
    // Arrange
    final var unitUnderTest = new IncomingHttpConnection(channel, networkBufferPool, httpRequestPool, "test");
    final var requestInput = "GET /cache/test HTTP/1.1 \r\nContent-Length: 16\r\n\r\nthis is the body";
    ByteLimitResponder.returnBytesFromMock(channel, requestInput, limit);

    for (var i = 0; i < requestInput.length(); i++) {
      unitUnderTest.progress();
    }

    // Assert
    unitUnderTest.cleanup();

    final var request = unitUnderTest.getRequest();
    assertThat(request).usingRecursiveComparison()
            .ignoringFields("requestBody")
            .isEqualTo(new HttpRequest(bufferSize)
                    .setMethod("GET")
                    .setRequestUri("/cache/test")
                    .setHttpVersion("HTTP/1.1")
                    .addHeader("content-length", "16"));

    ByteBufferAssertion.assertThat(request.getRequestBody()).isEqualTo("this is the body");
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 5, 8, 13, 50})
  void shouldMarkRequestAsCompleteWhenFinishedReadingWithNoHeaders(int limit, @Mock SocketChannel channel) throws IOException {
    // Arrange
    final var unitUnderTest = new IncomingHttpConnection(channel, networkBufferPool, httpRequestPool, "test");
    final var requestInput = "GET /cache/test HTTP/1.1 \r\n\r\n";
    ByteLimitResponder.returnBytesFromMock(channel, requestInput, limit);

    for (var i = 0; i < requestInput.length(); i++) {
      unitUnderTest.progress();
    }

    // Assert
    assertThat(unitUnderTest.progress()).isTrue();
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 5, 8, 13, 50})
  void shouldMarkRequestAsCompleteWhenFinishedReadingSingleHeader(int limit, @Mock SocketChannel channel) throws IOException {
    // Arrange
    final var unitUnderTest = new IncomingHttpConnection(channel, networkBufferPool, httpRequestPool, "test");
    final var requestInput = "GET /cache/test HTTP/1.1 \r\nExample-Header: 1234\r\n\r\n";
    ByteLimitResponder.returnBytesFromMock(channel, requestInput, limit);

    for (var i = 0; i < requestInput.length(); i++) {
      unitUnderTest.progress();
    }

    // Assert
    assertThat(unitUnderTest.progress()).isTrue();
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 5, 8, 13, 50})
  void shouldMarkRequestAsCompleteWhenFinishedReadingWithMultipleHeaders(int limit, @Mock SocketChannel channel) throws IOException {
    // Arrange
    final var unitUnderTest = new IncomingHttpConnection(channel, networkBufferPool, httpRequestPool, "test");
    final var requestInput = "GET /cache/test HTTP/1.1 \r\nExample-Header: 1234\r\nSecond-Header: hgj\r\n\r\n";
    ByteLimitResponder.returnBytesFromMock(channel, requestInput, limit);

    for (var i = 0; i < requestInput.length(); i++) {
      unitUnderTest.progress();
    }

    // Assert
    assertThat(unitUnderTest.progress()).isTrue();
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 5, 8, 13, 50})
  void shouldMarkRequestAsCompleteWhenFinishedReadingWithBody(int limit, @Mock SocketChannel channel) throws IOException {
    // Arrange
    final var unitUnderTest = new IncomingHttpConnection(channel, networkBufferPool, httpRequestPool, "test");
    final var requestInput = "POST /cache/test HTTP/1.1 \r\nContent-Length: 5\r\n\r\n12345";
    ByteLimitResponder.returnBytesFromMock(channel, requestInput, limit);

    for (var i = 0; i < requestInput.length(); i++) {
      unitUnderTest.progress();
    }

    // Assert
    assertThat(unitUnderTest.progress()).isTrue();
  }
}