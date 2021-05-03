package org.guardiandev.yak.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import org.guardiandev.yak.server.config.YakCacheConfig;
import org.guardiandev.yak.server.config.YakMemoryPoolBufferConfig;
import org.guardiandev.yak.server.config.YakMemoryPoolConfig;
import org.guardiandev.yak.server.config.YakServerConfig;
import org.guardiandev.yak.server.config.YakThreadIdleStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class HttpApiIntTest {

  private YakServerRunner yakServer;

  @BeforeEach
  void startServer() {
    final var config = new YakServerConfig()
            .setPort(9911)
            .setNetworkBufferPool(new YakMemoryPoolBufferConfig()
                    .setPoolSize(50)
                    .setFillOnCreation(true)
                    .setBufferSize(1024))
            .setHttpRequestMemoryPool(new YakMemoryPoolConfig()
                    .setPoolSize(50)
                    .setFillOnCreation(true))
            .setIncomingCacheRequestPool(new YakMemoryPoolBufferConfig()
                    .setPoolSize(50)
                    .setFillOnCreation(true)
                    .setBufferSize(256))
            .setThreadIdleStrategy(new YakThreadIdleStrategy()
                    .setMaxSpins(10)
                    .setMaxYields(10)
                    .setMinParkPeriodNs(10)
                    .setMaxParkPeriodNs(100))
            .setCaches(List.of(new YakCacheConfig()
                    .setName("intTest")
                    .setFixedValueSize(50)
                    .setEvictionStrategy("LRU")
                    .setMaximumKeys(32)
                    .setValueStorageMechanism("DirectMemoryStorage")));

    yakServer = new YakServerRunner(config);
    yakServer.init();
    yakServer.start();

    await().atMost(Duration.ofSeconds(5))
            .pollDelay(Duration.ofSeconds(1))
            .pollInterval(Duration.ofSeconds(1))
            .until(() -> yakServer.isRunning());
  }

  @AfterEach
  void stopServer() {
    yakServer.stop();
  }

  @Test
  void shouldReturnCreatedStatusWhenInsertingKey() throws IOException, InterruptedException {
    // Arrange
    final var client = HttpClient.newHttpClient();

    final var request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:9911/intTest/key-to-create"))
            .method("POST", HttpRequest.BodyPublishers.ofString("test-value"))
            .header("X-Request-Id", "insert-key-test")
            .version(HttpClient.Version.HTTP_1_1)
            .timeout(Duration.ofSeconds(10))
            .build();

    // Act
    final var response = client.send(request, HttpResponse.BodyHandlers.ofString());

    // Assert
    assertThat(response.statusCode()).isEqualTo(201);
    assertThat(response.body()).isNullOrEmpty();
  }

  @Test
  void shouldReturnOkStatusForGetOfExistingKeyWithKeyContents() throws IOException, InterruptedException {
    // Arrange
    final var client = HttpClient.newHttpClient();

    final var createRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:9911/intTest/key-to-create"))
            .method("POST", HttpRequest.BodyPublishers.ofString("test-value"))
            .header("X-Request-Id", "get-existing-key-test")
            .version(HttpClient.Version.HTTP_1_1)
            .timeout(Duration.ofSeconds(10))
            .build();

    final var createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
    assumeTrue(createResponse.statusCode() == 201);

    final var getRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:9911/intTest/key-to-create"))
            .header("X-Request-Id", "get-existing-key-test")
            .version(HttpClient.Version.HTTP_1_1)
            .timeout(Duration.ofSeconds(10))
            .build();

    // Act
    final var response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

    // Assert
    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isEqualTo("test-value");
  }

  @Test
  void shouldReturnNotFoundStatusForGetOfNonExistingKey() throws IOException, InterruptedException {
    // Arrange
    final var client = HttpClient.newHttpClient();

    final var request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:9911/intTest/non-existing-key"))
            .header("X-Request-Id", "get-non-existing-key-test")
            .version(HttpClient.Version.HTTP_1_1)
            .timeout(Duration.ofSeconds(10))
            .build();

    // Act
    final var response = client.send(request, HttpResponse.BodyHandlers.ofString());

    // Assert
    assertThat(response.statusCode()).isEqualTo(404);
    assertThat(response.body()).isNullOrEmpty();
  }

  @Test
  void shouldReturnNotFoundForGetOfNonExistingCache() throws IOException, InterruptedException {
    // Arrange
    final var client = HttpClient.newHttpClient();

    final var request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:9911/non-existing-cache/key"))
            .header("X-Request-Id", "get-non-existing-cache-test")
            .version(HttpClient.Version.HTTP_1_1)
            .timeout(Duration.ofSeconds(10))
            .build();

    // Act
    final var response = client.send(request, HttpResponse.BodyHandlers.ofString());

    // Assert
    assertThat(response.statusCode()).isEqualTo(404);
    assertThat(response.body()).isNullOrEmpty();
  }
}
