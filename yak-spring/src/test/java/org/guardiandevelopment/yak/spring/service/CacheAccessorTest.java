package org.guardiandevelopment.yak.spring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.Map;
import org.guardiandevelopment.yak.core.YakCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CacheAccessorTest {

  @Test
  void shouldReturnEmptyResultWhenCacheDoesNotExist() {
    // Arrange
    final var caches = Map.<String, YakCache<String, ByteBuffer>>of();
    final var underTest = new CacheAccessor(caches);

    // Act
    final var result = underTest.getValueInCache("non-existent-cache", "key");

    // Assert
    assertThat(result.isEmpty()).isTrue();
  }

  @Test
  void shouldReturnResultWhenCacheDoesExist(@Mock final YakCache<String, ByteBuffer> cache) {
    // Arrange
    final var resultBuffer = ByteBuffer.allocate(8);
    when(cache.get("key")).thenReturn(resultBuffer);
    final var underTest = new CacheAccessor(Map.of("test-cache", cache));

    // Act
    final var result = underTest.getValueInCache("test-cache", "key");

    // Assert
    assertThat(result.get()).isEqualTo(resultBuffer);
  }

  @Test
  void shouldReturnFalseOnInsertWhenCacheDoesNotExist() {
    // Arrange
    final var caches = Map.<String, YakCache<String, ByteBuffer>>of();
    final var underTest = new CacheAccessor(caches);

    // Act
    final var result = underTest.saveValueInCache("non-existent-cache", "key", "value");

    // Assert
    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnResultOfPutOnInsertWhenCacheDoesExist(@Mock final YakCache<String, ByteBuffer> cache) {
    // Arrange
    when(cache.put(eq("key"), any())).thenReturn(true);
    final var underTest = new CacheAccessor(Map.of("test-cache", cache));

    // Act
    final var result = underTest.saveValueInCache("test-cache", "key", "value");

    // Assert
    assertThat(result).isTrue();
  }
}