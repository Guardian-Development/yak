package yak;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import yak.reference.TestCacheValue;
import yak.reference.TestCacheValueSerializer;

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
}
