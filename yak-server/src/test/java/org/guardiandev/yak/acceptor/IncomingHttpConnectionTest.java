package org.guardiandev.yak.acceptor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.guardiandev.yak.pool.Factory;
import org.guardiandev.yak.pool.MemoryPool;
import org.guardiandev.yak.testsupport.ByteLimitResponder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IncomingHttpConnectionTest {

  private final MemoryPool<ByteBuffer> networkBufferPool = Factory.networkBufferPool(2, 256, true);
  private final MemoryPool<HttpRequest> httpRequestPool = Factory.httpRequestPool(2, true);
  private final MemoryPool<IncomingCacheRequest> incomingCacheRequestPool = Factory.incomingCacheRequestPool(2, 256, true);

  @Test
  void shouldExtractHttpRequestLineFromRequest(@Mock SocketChannel channel) throws IOException {
    // Arrange
    final var unitUnderTest = new IncomingHttpConnection(channel, networkBufferPool, httpRequestPool, incomingCacheRequestPool);
    ByteLimitResponder.returnBytesFromMock(channel, "GET /cache/test HTTP/1.1 \r\n");

    // Act
    unitUnderTest.progress();

    // Assert
    unitUnderTest.cleanup();

    httpRequestPool.take();
    final var request = httpRequestPool.take();
    assertThat(request).usingRecursiveComparison()
            .isEqualTo(new HttpRequest()
                    .setMethod("GET")
                    .setRequestUri("/cache/test")
                    .setHttpVersion("HTTP/1.1"));
  }

  @Test
  void shouldExtractHttpRequestLineFromRequestWithMultipleSocketReads(@Mock SocketChannel channel) throws IOException {
    // Arrange
    final var unitUnderTest = new IncomingHttpConnection(channel, networkBufferPool, httpRequestPool, incomingCacheRequestPool);
    final var requestInput = "GET /cache/test HTTP/1.1 \r\n";
    ByteLimitResponder.returnBytesFromMock(channel, requestInput, 1);

    // Act - 27 bytes total in message, 1 byte read per iteration
    for (var i = 0; i < requestInput.length(); i++) {
      unitUnderTest.progress();
    }

    // Assert
    unitUnderTest.cleanup();

    httpRequestPool.take();
    final var request = httpRequestPool.take();
    assertThat(request).usingRecursiveComparison()
            .isEqualTo(new HttpRequest()
                    .setMethod("GET")
                    .setRequestUri("/cache/test")
                    .setHttpVersion("HTTP/1.1"));
  }
}