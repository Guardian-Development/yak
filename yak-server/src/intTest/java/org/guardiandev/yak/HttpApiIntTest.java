package org.guardiandev.yak;

import org.guardiandev.yak.config.YakCacheConfig;
import org.guardiandev.yak.config.YakServerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

final class HttpApiIntTest {

    private YakServerRunner yakServer;

    @BeforeEach
    void startServer() {
        final var config = new YakServerConfig()
                .setPort(9911)
                .setCaches(List.of(new YakCacheConfig()
                        .setName("intTest")
                        .setFixedValueSize(50)
                        .setEvictionStrategy("LRU")
                        .setMaximumKeys(32)
                        .setValueStorageMechanism("DirectMemoryStorage")));

        yakServer = new YakServerRunner(config);
        yakServer.init();
        yakServer.start();

        await().atMost(Duration.ofSeconds(5)).until(() -> yakServer.isRunning());
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
    void shouldReturnNotFoundStatusForNonExistingKey() throws IOException, InterruptedException {
        // Arrange
        final var client = HttpClient.newHttpClient();

        final var request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9911/intTest/non-existing-key"))
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
