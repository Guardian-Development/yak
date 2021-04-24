package org.guardiandev.yak.acceptor;

import org.guardiandev.yak.pool.Factory;
import org.guardiandev.yak.pool.MemoryPool;
import org.guardiandev.yak.testsupport.ByteLimitResponder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class IncomingHttpConnectionTest {

  private final MemoryPool<ByteBuffer> networkBufferPool = Factory.networkBufferPool(1, 256, true);
  private final MemoryPool<HttpRequest> httpRequestPool = Factory.httpRequestPool(1, true);
  private final MemoryPool<IncomingCacheRequest> incomingCacheRequestPool = Factory.incomingCacheRequestPool(1, 256, true);

  @Test
  void shouldProcessRequestLineAsContentUpToFirstCRLF(@Mock SocketChannel channel) throws IOException {
    // Arrange
    final var unitUnderTest = new IncomingHttpConnection(channel, networkBufferPool, httpRequestPool, incomingCacheRequestPool);
    ByteLimitResponder.returnBytesFromMock(channel, "GET /cache/test HTTP/1.1 \r\n");

    // Act
    unitUnderTest.progress();

    // Assert
    unitUnderTest.cleanup();
    final var request = httpRequestPool.take();
    assertThat(request).usingRecursiveComparison()
            .isEqualTo(new HttpRequest()
                    .setMethod("GET")
                    .setRequestUri("/cache/test")
                    .setHttpVersion("HTTP/1.1"));
  }

  @Test
  void shouldProcessRequestLineAsContentUpToFirstCRLFWithMultipleSocketReads(@Mock SocketChannel channel) throws IOException {
    // Arrange
    final var unitUnderTest = new IncomingHttpConnection(channel, networkBufferPool, httpRequestPool, incomingCacheRequestPool);
    ByteLimitResponder.returnBytesFromMock(channel, "GET /cache/test HTTP/1.1 \r\n", 5);

    // Act - 27 bytes to write, limit to 5 bytes per write
    unitUnderTest.progress();
    unitUnderTest.progress();
    unitUnderTest.progress();
    unitUnderTest.progress();
    unitUnderTest.progress();
    unitUnderTest.progress();

    // Assert
    unitUnderTest.cleanup();
    final var request = httpRequestPool.take();
    assertThat(request).usingRecursiveComparison()
            .isEqualTo(new HttpRequest()
                    .setMethod("GET")
                    .setRequestUri("/cache/test")
                    .setHttpVersion("HTTP/1.1"));
  }
}