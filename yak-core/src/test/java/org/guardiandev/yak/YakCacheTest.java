package org.guardiandev.yak;

import static org.assertj.core.api.Assertions.assertThat;

import org.agrona.concurrent.UnsafeBuffer;
import org.guardiandev.yak.reference.TestCacheValue;
import org.guardiandev.yak.reference.TestCacheValueSerializer;
import org.guardiandev.yak.serialization.YakValueSerializer;
import org.junit.jupiter.api.Test;

class YakCacheTest {

  @Test
  void shouldBeAbleToReadBackValueForPresentKey() {
    // Arrange
    final var unitUnderTest = YakCache.<String, TestCacheValue>newBuilder()
        .fixedValueSize(TestCacheValueSerializer.MAXIMUM_SUPPORTED_SIZE_BYTES)
        .maximumKeys(16)
        .valueSerializer(new TestCacheValueSerializer())
        .build();

    final var cacheValue = new TestCacheValue();
    cacheValue.id = 172;
    cacheValue.field1 = "test me 123";

    // Act
    unitUnderTest.put("custom-key-1", cacheValue);
    final var result = unitUnderTest.get("custom-key-1");

    // Assert
    assertThat(result)
        .usingRecursiveComparison()
        .isEqualTo(cacheValue);
  }

  @Test
  void shouldBeAbleToReadBackMultipleKeyValues() {
    // Arrange
    final int numberOfKeys = 4096;
    final var unitUnderTest = YakCache.<Integer, Integer>newBuilder()
        .fixedValueSize(50)
        .maximumKeys(numberOfKeys)
        .valueSerializer(new YakValueSerializer<>() {
          @Override
          public int serialize(Integer value, UnsafeBuffer valueBuffer) {
            valueBuffer.putInt(0, value);
            return Integer.BYTES;
          }

          @Override
          public Integer deserialize(UnsafeBuffer valueBuffer) {
            return valueBuffer.getInt(0);
          }
        })
        .build();

    // Act
    for (int i = 0; i < numberOfKeys; i++) {
      unitUnderTest.put(i, i);
    }

    // Assert
    for (int i = 0; i < numberOfKeys; i++) {
      assertThat(unitUnderTest.get(i)).isEqualTo(i);
    }
  }

  @Test
  void shouldBeAbleToReadBackMultipleKeyValuesWhenKeySizeExceedsCacheSize() {
    // Arrange
    final int numberOfKeys = 16384;
    final int cacheSize = 4096;
    final var unitUnderTest = YakCache.<Integer, Integer>newBuilder()
            .fixedValueSize(50)
            .maximumKeys(cacheSize)
            .valueSerializer(new YakValueSerializer<>() {
              @Override
              public int serialize(Integer value, UnsafeBuffer valueBuffer) {
                valueBuffer.putInt(0, value);
                return Integer.BYTES;
              }

              @Override
              public Integer deserialize(UnsafeBuffer valueBuffer) {
                return valueBuffer.getInt(0);
              }
            })
            .build();

    // Act
    for (int i = 0; i < numberOfKeys; i++) {
      unitUnderTest.put(i, i);
    }

    // Assert - with LRU eviction should be left with last 1024 keys inserted
    final int remainingKeys = numberOfKeys - cacheSize;
    for (int i = remainingKeys; i < numberOfKeys; i++) {
      assertThat(unitUnderTest.get(i)).isEqualTo(i);
    }
  }
}
