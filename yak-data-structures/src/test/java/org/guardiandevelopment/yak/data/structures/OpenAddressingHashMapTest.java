package org.guardiandevelopment.yak.data.structures;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OpenAddressingHashMapTest {

  @Test
  void shouldBeAbleToGetValueAssociatedWithKey() {
    // Arrange
    final var underTest = new OpenAddressingHashMap<Integer, String>(4);

    underTest.put(2, "test");

    // Act
    final var result = underTest.get(2);

    // Assert
    assertThat(result).isEqualTo("test");
  }

  @Test
  void shouldBeAbleToFillHashMapToCapacityWithoutLosingAnyValues() {
    // Arrange
    final var underTest = new OpenAddressingHashMap<Integer, String>(4);

    underTest.put(1, "test1");
    underTest.put(2, "test2");
    underTest.put(3, "test3");
    underTest.put(4, "test4");

    // Act
    final var result1 = underTest.get(1);
    final var result2 = underTest.get(2);
    final var result3 = underTest.get(3);
    final var result4 = underTest.get(4);

    // Assert
    assertThat(result1).isEqualTo("test1");
    assertThat(result2).isEqualTo("test2");
    assertThat(result3).isEqualTo("test3");
    assertThat(result4).isEqualTo("test4");
  }

  @Test
  void shouldBeAbleToHandleHashCollisions() {
    // Arrange - 2 key objects will have same hash value but not equal causing collision
    final var underTest = new OpenAddressingHashMap<HashCollider, String>(4);

    final var keyOne = new HashCollider(2);
    final var keyTwo = new HashCollider(2);

    underTest.put(keyOne, "test1");
    underTest.put(keyTwo, "test2");

    // Act
    final var result1 = underTest.get(keyOne);
    final var result2 = underTest.get(keyTwo);

    // Assert
    assertThat(result1).isEqualTo("test1");
    assertThat(result2).isEqualTo("test2");
  }

  @Test
  void shouldReturnOldValueOnDelete() {
    // Arrange
    final var underTest = new OpenAddressingHashMap<Integer, String>(4);

    underTest.put(1, "test1");

    // Act
    final var old = underTest.delete(1);

    // Assert
    assertThat(old).isEqualTo("test1");
  }

  @Test
  void shouldBeAbleToReplaceValueAssociatedWithKey() {
    // Arrange
    final var underTest = new OpenAddressingHashMap<Integer, String>(4);

    underTest.put(1, "test1");

    // Act
    final var old = underTest.put(1, "test2");
    final var newValue = underTest.get(1);

    // Assert
    assertThat(old).isEqualTo("test1");
    assertThat(newValue).isEqualTo("test2");
  }

  @Test
  void shouldReturnNullWhenDeletingNonExistingKey() {
    // Arrange
    final var underTest = new OpenAddressingHashMap<Integer, String>(4);

    // Act
    final var old = underTest.delete(1);

    // Assert
    assertThat(old).isNull();
  }

  @Test
  void shouldReturnOldValueWhenDeletingHashCollidedKey() {
    // Arrange - 2 key objects will have same hash value but not equal causing collision
    final var underTest = new OpenAddressingHashMap<HashCollider, String>(4);

    final var keyOne = new HashCollider(2);
    final var keyTwo = new HashCollider(2);

    underTest.put(keyOne, "test1");
    underTest.put(keyTwo, "test2");

    // Act
    final var result1 = underTest.delete(keyOne);
    final var result2 = underTest.delete(keyTwo);

    // Assert
    assertThat(result1).isEqualTo("test1");
    assertThat(result2).isEqualTo("test2");
  }

  @Test
  void shouldBeAbleToUseDeletedSlotForNewKey() {
    // Arrange
    final var underTest = new OpenAddressingHashMap<Integer, String>(4);

    underTest.put(1, "test1");
    underTest.put(2, "test2");
    underTest.put(3, "test3");
    underTest.put(4, "test4");

    underTest.delete(2);
    underTest.delete(3);
    underTest.put(2, "testNew2");
    underTest.put(3, "testNew3");

    // Act
    final var result1 = underTest.get(1);
    final var result2 = underTest.get(2);
    final var result3 = underTest.get(3);
    final var result4 = underTest.get(4);

    // Assert
    assertThat(result1).isEqualTo("test1");
    assertThat(result2).isEqualTo("testNew2");
    assertThat(result3).isEqualTo("testNew3");
    assertThat(result4).isEqualTo("test4");
  }

  @Test
  void shouldBeAbleToUseDeletedSlotForNewKeyWithHashCollidingKeys() {
    // Arrange
    final var underTest = new OpenAddressingHashMap<HashCollider, String>(4);

    final var key1 = new HashCollider(1);
    final var key2 = new HashCollider(1);
    final var key3 = new HashCollider(2);
    final var key4 = new HashCollider(2);

    underTest.put(key1, "test1");
    underTest.put(key2, "test2");
    underTest.put(key3, "test3");
    underTest.put(key4, "test4");

    underTest.delete(key2);
    underTest.delete(key3);
    underTest.put(key2, "testNew2");
    underTest.put(key3, "testNew3");

    // Act
    final var result1 = underTest.get(key1);
    final var result2 = underTest.get(key2);
    final var result3 = underTest.get(key3);
    final var result4 = underTest.get(key4);

    // Assert
    assertThat(result1).isEqualTo("test1");
    assertThat(result2).isEqualTo("testNew2");
    assertThat(result3).isEqualTo("testNew3");
    assertThat(result4).isEqualTo("test4");
  }

  private record HashCollider(int hashCodeValue) {

    @Override
    public boolean equals(Object o) {
      return this == o;
    }

    @Override
    public int hashCode() {

      return hashCodeValue;
    }
  }
}
