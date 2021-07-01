package org.guardiandevelopment.yak.spring.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class YakCacheProviderTest {

  @Test
  void shouldConfigureNoCaches(@Mock final YakCacheMetrics metrics) {
    // Arrange
    final var config = new YakServerConfig().setCaches(Collections.emptyList());
    final var underTest = new YakCacheProvider(config, metrics);

    // Act
    final var result = underTest.caches();

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void shouldConfigureEachCacheInConfig(@Mock final YakCacheMetrics metrics) {
    // Arrange
    final var config = new YakServerConfig().setCaches(List.of(
            new YakCacheConfig()
                    .setName("test-1")
                    .setFixedValueSize(8)
                    .setEvictionStrategy("LRU")
                    .setMaximumKeys(16)
                    .setValueStorageMechanism("DirectMemoryStorage"),
            new YakCacheConfig()
                    .setName("test-2")
                    .setFixedValueSize(8)
                    .setEvictionStrategy("LRU")
                    .setMaximumKeys(16)
                    .setValueStorageMechanism("DirectMemoryStorage")));
    final var underTest = new YakCacheProvider(config, metrics);

    // Act
    final var result = underTest.caches();

    // Assert
    assertThat(result).containsOnlyKeys("test-1", "test-2");
  }

}