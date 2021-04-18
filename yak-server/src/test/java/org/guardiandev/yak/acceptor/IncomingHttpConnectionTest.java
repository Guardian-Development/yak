package org.guardiandev.yak.acceptor;

import org.guardiandev.yak.testsupport.ByteLimitResponder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class IncomingHttpConnectionTest {

  @Test
  void shouldProcessRequestLineAsContentUpToFirstCRLF(@Mock SocketChannel channel) throws IOException {
    // Arrange
    final var request = new HttpRequest();
    final var unitUnderTest = new IncomingHttpConnection(channel, request);
    ByteLimitResponder.returnBytesFromMock(channel, "GET /cache/test HTTP/1.1 \r\n");

    // Act
    unitUnderTest.progress();

    // Assert
    assertThat(request).usingRecursiveComparison()
            .isEqualTo(new HttpRequest()
                    .setMethod("GET")
                    .setRequestUri("/cache/test")
                    .setHttpVersion("HTTP/1.1"));
  }

  @Test
  void shouldProcessRequestLineAsContentUpToFirstCRLFWithMultipleSocketReads(@Mock SocketChannel channel) throws IOException {
    // Arrange
    final var request = new HttpRequest();
    final var unitUnderTest = new IncomingHttpConnection(channel, request);
    ByteLimitResponder.returnBytesFromMock(channel, "GET /cache/test HTTP/1.1 \r\n", 5);

    // Act - 27 bytes to write, limit to 5 bytes per write
    unitUnderTest.progress();
    unitUnderTest.progress();
    unitUnderTest.progress();
    unitUnderTest.progress();
    unitUnderTest.progress();
    unitUnderTest.progress();

    // Assert
    assertThat(request).usingRecursiveComparison()
            .isEqualTo(new HttpRequest()
                    .setMethod("GET")
                    .setRequestUri("/cache/test")
                    .setHttpVersion("HTTP/1.1"));
  }
}