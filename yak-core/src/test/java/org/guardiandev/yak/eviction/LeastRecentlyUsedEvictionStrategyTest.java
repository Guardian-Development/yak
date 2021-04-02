package org.guardiandev.yak.eviction;

import static org.assertj.core.api.Assertions.assertThat;

import org.guardiandev.yak.events.YakEventListener;
import org.junit.jupiter.api.Test;

class LeastRecentlyUsedEvictionStrategyTest {

  private final LeastRecentlyUsedEvictionStrategy<String> underTest = new LeastRecentlyUsedEvictionStrategy<>();

  @Test
  void shouldRemoveOnlyKeyOnEviction() {
    // Arrange
    underTest.init(10);

    underTest.accept(YakEventListener.YakEvent.PUT, "test-key", "test-value");

    // Act
    final var result = underTest.keyToEvict();

    // Assert
    assertThat(result).isEqualTo("test-key");
  }

  @Test
  void shouldRemoveLastRecentlySeenKeyOnMultipleEvictions() {
    // Arrange
    underTest.init(5);

    underTest.accept(YakEventListener.YakEvent.GET_CACHE_HIT, "test-1", "");
    underTest.accept(YakEventListener.YakEvent.GET_CACHE_HIT, "test-2", "");
    underTest.accept(YakEventListener.YakEvent.PUT, "test-3", "");
    underTest.accept(YakEventListener.YakEvent.GET_CACHE_HIT, "test-4", "");
    underTest.accept(YakEventListener.YakEvent.GET_CACHE_HIT, "test-5", "");

    // Act
    // Assert
    assertThat(underTest.keyToEvict()).isEqualTo("test-1");
    assertThat(underTest.keyToEvict()).isEqualTo("test-2");
    assertThat(underTest.keyToEvict()).isEqualTo("test-3");
    assertThat(underTest.keyToEvict()).isEqualTo("test-4");
    assertThat(underTest.keyToEvict()).isEqualTo("test-5");
  }

  @Test
  void shouldIgnoreCacheMissEvents() {
    // Arrange
    underTest.init(5);

    underTest.accept(YakEventListener.YakEvent.GET_CACHE_HIT, "test-1", "");
    underTest.accept(YakEventListener.YakEvent.GET_CACHE_MISS, "test-2", "");
    underTest.accept(YakEventListener.YakEvent.PUT, "test-3", "");
    underTest.accept(YakEventListener.YakEvent.GET_CACHE_MISS, "test-4", "");
    underTest.accept(YakEventListener.YakEvent.GET_CACHE_HIT, "test-5", "");

    // Act
    // Assert
    assertThat(underTest.keyToEvict()).isEqualTo("test-1");
    assertThat(underTest.keyToEvict()).isEqualTo("test-3");
    assertThat(underTest.keyToEvict()).isEqualTo("test-5");
  }
}