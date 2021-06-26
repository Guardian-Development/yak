package org.guardiandevelopment.yak.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class YakApplicationTests {

  @LocalServerPort
  private int port;

  @Test
  void shouldReturnHealthyForHealthCheck() throws IOException, InterruptedException {
    // Arrange
    final var client = HttpClient.newHttpClient();

    final var request = HttpRequest.newBuilder()
            .uri(URI.create(String.format("http://localhost:%d/actuator/health", port)))
            .header("X-Request-Id", "health-check-test")
            .version(HttpClient.Version.HTTP_1_1)
            .timeout(Duration.ofSeconds(10))
            .build();

    // Act
    final var response = client.send(request, HttpResponse.BodyHandlers.ofString());

    // Assert
    assertThat(response.statusCode()).isEqualTo(200);
  }

  @Test
  void shouldReturnCreatedStatusWhenInsertingKey() throws IOException, InterruptedException {
    // Arrange
    final var client = HttpClient.newHttpClient();

    final var request = HttpRequest.newBuilder()
            .uri(URI.create(String.format("http://localhost:%d/cache/intTest/key-to-create", port)))
            .method("POST", HttpRequest.BodyPublishers.ofString("test-value"))
            .header("X-Request-Id", "insert-key-test")
            .header("Content-Type", "application/json")
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
            .uri(URI.create(String.format("http://localhost:%d/cache/intTest/key-to-create", port)))
            .method("POST", HttpRequest.BodyPublishers.ofString("test-value"))
            .header("X-Request-Id", "get-existing-key-test")
            .header("Content-Type", "application/json")
            .version(HttpClient.Version.HTTP_1_1)
            .timeout(Duration.ofSeconds(10))
            .build();

    final var createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
    assumeTrue(createResponse.statusCode() == 201);

    final var getRequest = HttpRequest.newBuilder()
            .uri(URI.create(String.format("http://localhost:%d/cache/intTest/key-to-create", port)))
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
            .uri(URI.create(String.format("http://localhost:%d/cache/intTest/non-existing-key", port)))
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
            .uri(URI.create(String.format("http://localhost:%d/cache/non-existing-cache/key", port)))
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
