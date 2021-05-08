package org.guardiandev.yak.core.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.guardiandev.yak.core.storage.DirectMemoryStorage;
import org.junit.jupiter.api.Test;

class DirectMemoryStorageTest {

  private final DirectMemoryStorage unitUnderTest = new DirectMemoryStorage();

  @Test
  void shouldAllocateUnsafeBufferPerIndexInContinuousMemory() {
    // Arrange
    final var numberOfEntries = 10;
    final var sizeOfEntry = Long.BYTES;
    unitUnderTest.init(numberOfEntries, sizeOfEntry);

    // Assert
    var startingOffset = unitUnderTest.getStorage(0).addressOffset();
    for (var i = 0; i < numberOfEntries; i++) {
      final var expectedOffset = startingOffset + (i * sizeOfEntry);
      final var buffer = unitUnderTest.getStorage(i);
      assertThat(buffer.addressOffset()).isEqualTo(expectedOffset);
    }
  }

  @Test
  void shouldThrowExceptionIfIndexLessThanZero() {
    // Arrange
    final var numberOfEntries = 100;
    final var sizeOfEntry = 12;
    unitUnderTest.init(numberOfEntries, sizeOfEntry);

    // Assert
    assertThatThrownBy(() -> unitUnderTest.getStorage(-1))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowExceptionIfIndexGreaterThanMaximumNumberOfKeys() {
    // Arrange
    final var numberOfEntries = 100;
    final var sizeOfEntry = 12;
    unitUnderTest.init(numberOfEntries, sizeOfEntry);

    // Act
    final var lastBuffer = unitUnderTest.getStorage(numberOfEntries - 1);

    // Assert
    assertThat(lastBuffer).isNotNull();
    assertThatThrownBy(() -> unitUnderTest.getStorage(numberOfEntries))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldBeAbleToWriteAndReadValueAtIndex() {
    // Arrange
    final var numberOfEntries = 3;
    final var sizeOfEntry = Integer.BYTES;
    unitUnderTest.init(numberOfEntries, sizeOfEntry);

    final var valueToWrite = 12;

    // Act
    final var buffer = unitUnderTest.getStorage(0);
    buffer.putInt(0, valueToWrite);

    final var sameBuffer = unitUnderTest.getStorage(0);
    final var result = sameBuffer.getInt(0);

    // Assert
    assertThat(result).isEqualTo(valueToWrite);
  }

}