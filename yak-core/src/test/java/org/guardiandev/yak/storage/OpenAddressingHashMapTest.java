package org.guardiandev.yak.storage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OpenAddressingHashMapTest {

  @Test
  void shouldInsertKeyIntoStartingLocationIfEmpty() {
    // Arrange - 16 % 16 = location 0
    final var underTest = new OpenAddressingHashMap<FixedHashCodeOf>(16);
    final var key = new FixedHashCodeOf(16);

    // Act
    final var location = underTest.getExistingOrAssign(key);

    // Assert
    assertThat(location).isEqualTo(0);
  }

  @Test
  void shouldInsertKeyIntoStartingLocationIfExistingKeyEqualToKey() {
    // Arrange - 16 % 16 = location 0
    final var underTest = new OpenAddressingHashMap<FixedHashCodeOf>(16);
    final var key = new FixedHashCodeOf(16);

    underTest.getExistingOrAssign(key);

    // Act
    final var location = underTest.getExistingOrAssign(key);

    // Assert
    assertThat(location).isEqualTo(0);
  }

  @Test
  void shouldInsertKeyInNextEmptyPositionIfInitialPositionTaken() {
    // Arrange
    final var underTest = new OpenAddressingHashMap<FixedHashCodeOf>(8);
    final var key1 = new FixedHashCodeOf(8);  // 8 % 8 = location 0
    final var key2 = new FixedHashCodeOf(16); // 16 % 16 = location 0

    underTest.getExistingOrAssign(key1);

    // Act - 1 + (16 % 7) = 3
    // 0 + 3 = next position 3
    final var location = underTest.getExistingOrAssign(key2);

    // Assert
    assertThat(location).isEqualTo(3);
  }

  @Test
  void shouldInsertKeyInNextPositionContainingKeyIfInitialPositionTaken() {
    // Arrange
    final var underTest = new OpenAddressingHashMap<FixedHashCodeOf>(8);
    final var key1 = new FixedHashCodeOf(8);  // 8 % 8 = location 0
    final var key2 = new FixedHashCodeOf(16); // 16 % 16 = location 0

    underTest.getExistingOrAssign(key1);
    underTest.getExistingOrAssign(key2);

    // Act - 1 + (16 % 7) = 3
    // 0 + 3 = next position 3
    final var location = underTest.getExistingOrAssign(key2);

    // Assert
    assertThat(location).isEqualTo(3);
  }

  @Test
  void shouldInsertKeyInOriginalMarkedDeletedPositionIfNoEmptyOrExistingLocationsAvailable() {
    // Arrange
    final var underTest = new OpenAddressingHashMap<FixedHashCodeOf>(4);
    final var key1 = new FixedHashCodeOf(0); // 0 % 4 = location 0
    final var key2 = new FixedHashCodeOf(1); // 1 % 4 = location 1
    final var key3 = new FixedHashCodeOf(2); // 2 % 4 = location 2
    final var key4 = new FixedHashCodeOf(3); // 3 % 4 = location 3

    underTest.getExistingOrAssign(key1);
    underTest.getExistingOrAssign(key2);
    underTest.getExistingOrAssign(key3);
    underTest.getExistingOrAssign(key4);

    underTest.delete(key3);

    final var newKey = new FixedHashCodeOf(6); // 6 % 4 = location 2

    // Act
    final var location = underTest.getExistingOrAssign(newKey);

    // Assert
    assertThat(location).isEqualTo(2);
  }

  @Test
  void shouldInsertKeyInFirstDeletedPositionIfNoEmptyOrExistingLocationsAvailable() {
    // Arrange
    final var underTest = new OpenAddressingHashMap<FixedHashCodeOf>(4);
    final var key1 = new FixedHashCodeOf(0); // 0 % 4 = location 0
    final var key2 = new FixedHashCodeOf(1); // 1 % 4 = location 1
    final var key3 = new FixedHashCodeOf(2); // 2 % 4 = location 2
    final var key4 = new FixedHashCodeOf(3); // 3 % 4 = location 3

    underTest.getExistingOrAssign(key1);
    underTest.getExistingOrAssign(key2);
    underTest.getExistingOrAssign(key3);
    underTest.getExistingOrAssign(key4);

    underTest.delete(key4);

    final var newKey = new FixedHashCodeOf(6); // 6 % 4 = location 2

    // Act
    final var location = underTest.getExistingOrAssign(newKey);

    // Assert
    assertThat(location).isEqualTo(3);
  }

  @Test
  void shouldReturnNullIfAllPositionsTakenAndNoDeletedPositionsAvailable() {
    // Arrange
    final var underTest = new OpenAddressingHashMap<FixedHashCodeOf>(4);
    final var key1 = new FixedHashCodeOf(0); // 0 % 4 = location 0
    final var key2 = new FixedHashCodeOf(1); // 1 % 4 = location 1
    final var key3 = new FixedHashCodeOf(2); // 2 % 4 = location 2
    final var key4 = new FixedHashCodeOf(3); // 3 % 4 = location 3

    underTest.getExistingOrAssign(key1);
    underTest.getExistingOrAssign(key2);
    underTest.getExistingOrAssign(key3);
    underTest.getExistingOrAssign(key4);

    final var newKey = new FixedHashCodeOf(6); // 6 % 4 = location 2

    // Act
    final var location = underTest.getExistingOrAssign(newKey);

    // Assert
    assertThat(location).isNull();
  }

  @Test
  void shouldReturnTrueWhenDeletingExistingKeyInStartingLocation() {
    // Arrange
    final var underTest = new OpenAddressingHashMap<FixedHashCodeOf>(4);
    final var key1 = new FixedHashCodeOf(0); // 0 % 4 = location 0

    underTest.getExistingOrAssign(key1);

    // Act
    final var deleted = underTest.delete(key1);

    // Assert
    assertThat(deleted).isTrue();
  }

  @Test
  void shouldReturnTrueWhenDeletingExistingKeyNotInStartingLocation() {
    // Arrange
    final var underTest = new OpenAddressingHashMap<FixedHashCodeOf>(4);
    final var key1 = new FixedHashCodeOf(2); // 2 % 4 = location 2
    final var key2 = new FixedHashCodeOf(6); // 6 % 4 = location 2

    underTest.getExistingOrAssign(key1);
    underTest.getExistingOrAssign(key2);

    // Act - 1 + (6 % 3) = 1
    // (2 + 1) % 4 = next position 3
    final var deleted = underTest.delete(key2);

    // Assert
    assertThat(deleted).isTrue();
  }

  @Test
  void shouldReturnTrueWhenKeyDoesNotExistAndHitEmptySlot() {
    // Arrange
    final var underTest = new OpenAddressingHashMap<FixedHashCodeOf>(4);
    final var key1 = new FixedHashCodeOf(2); // 2 % 4 = location 2
    final var key2 = new FixedHashCodeOf(6); // 6 % 4 = location 2

    underTest.getExistingOrAssign(key1);

    // Act - 1 + (6 % 3) = 1
    // (2 + 1) % 4 = next position 3
    final var deleted = underTest.delete(key2);

    // Assert
    assertThat(deleted).isTrue();
  }

  @Test
  void shouldReturnTrueWhenKeyDoesNotExistAndWeCheckAllPossibleLocations() {
    // Arrange
    final var underTest = new OpenAddressingHashMap<FixedHashCodeOf>(4);
    final var key1 = new FixedHashCodeOf(2);  // 2 % 4 = location 2
    final var key2 = new FixedHashCodeOf(6);  // 6 % 4 = location 2
    final var key3 = new FixedHashCodeOf(10); // 10 % 4 = location 2
    final var key4 = new FixedHashCodeOf(14); // 14 % 4 = location 2

    underTest.getExistingOrAssign(key1);
    underTest.getExistingOrAssign(key2);
    underTest.getExistingOrAssign(key3);
    underTest.getExistingOrAssign(key4);

    // Act - 1 + (18 % 3) = 1
    // (2 + 1) % 4 = next position 3, 0, 1, 2
    final var nonExistingKey = new FixedHashCodeOf(18); // 18 % 4 = location 2

    // Act
    final var deleted = underTest.delete(nonExistingKey);

    // Assert
    assertThat(deleted).isTrue();
  }

  private static final class FixedHashCodeOf {

    private final int hashCodeValue;

    public FixedHashCodeOf(final int hashCodeValue) {
      this.hashCodeValue = hashCodeValue;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      FixedHashCodeOf testKey = (FixedHashCodeOf) o;
      return hashCodeValue == testKey.hashCodeValue;
    }

    @Override
    public int hashCode() {
      return hashCodeValue;
    }
  }
}