package org.guardiandev.yak.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class YakConfigFromJsonBuilderTest {

  @Test
  void shouldLoadGoodConfigSingleCache() {
    // Arrange
    final var expectedConfig = new YakServerConfig()
            .setPort(9000)
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
            .setCaches(List.of(new YakCacheConfig()
                    .setName("example1")
                    .setMaximumKeys(64)
                    .setFixedValueSize(128)
                    .setValueStorageMechanism("DirectMemoryStorage")
                    .setEvictionStrategy("LRU")));

    final var unitUnderTest = new YakConfigFromJsonBuilder(Path.of("src/test/resources/config/good-yak-config.json"));

    // Act
    final var result = unitUnderTest.load();

    // Assert
    assertThat(result)
            .usingRecursiveComparison()
            .isEqualTo(expectedConfig);
  }

  @Test
  void shouldLoadConfigMissingValues() {
    // Arrange
    final var expectedConfig = new YakServerConfig()
            .setCaches(List.of(new YakCacheConfig()));

    final var unitUnderTest = new YakConfigFromJsonBuilder(Path.of("src/test/resources/config/missing-values-yak-config.json"));

    // Act
    final var result = unitUnderTest.load();

    // Assert
    assertThat(result)
            .usingRecursiveComparison()
            .isEqualTo(expectedConfig);
  }

  @Test
  void shouldLoadConfigNoValuesPresent() {
    // Arrange
    final var expectedConfig = new YakServerConfig();

    final var unitUnderTest = new YakConfigFromJsonBuilder(Path.of("src/test/resources/config/no-values-yak-config.json"));

    // Act
    final var result = unitUnderTest.load();

    // Assert
    assertThat(result)
            .usingRecursiveComparison()
            .isEqualTo(expectedConfig);
  }
}