package org.guardiandevelopment.yak.data.structures;

/**
 * Provides a fixed size HashMap implementation using open addressing collision resolution technique.
 *
 * @param <K> the type of the key
 * @param <V> the type of the value
 */
public final class OpenAddressingHashMap<K, V> {

  private record Node<K, V>(K key, V value) {
  }

  private final Node<K, V> deleted = new Node<>(null, null);
  private final Node<K, V>[] values;

  // m and m', where m` = m - 1
  // h1(k) = k mod m = initial location
  // h2(k) = 1 + (k mod m') = increment between locations
  private final int keySpace;
  private final int mprime;

  /**
   * Creates a hash map of the fixedSize using the open addressing collision resolution technique.
   *
   * <p>
   * The fixed size must be a power of 2, as this allows for better resolutions of collisions
   * as we use Double Hashing when probing the table.
   * </p>
   *
   * @param fixedSize the size of the hash map
   */
  @SuppressWarnings("unchecked")
  public OpenAddressingHashMap(final int fixedSize) {

    assert IntegerExtensions.isPowerOf2(fixedSize) : "the size of the hashmap must be a power of 2";

    this.values = (Node<K, V>[]) new Node[fixedSize];
    this.mprime = fixedSize - 1;
    this.keySpace = values.length;
  }

  /**
   * Searches for the location of the key within the hash map.
   *
   * @param key the key to search for
   * @return the location of the key if it exists, else null
   */
  public V get(final K key) {

    if (key == null) {
      return null;
    }

    final var hash = key.hashCode();
    final var position = Math.abs(hash % keySpace);
    final var currentEntryAtPosition = values[position];

    // if location is empty, return null
    if (currentEntryAtPosition == null) {
      return null;
    }

    if (currentEntryAtPosition.key().equals(key)) {
      return currentEntryAtPosition.value();
    }

    // collision happened, search for potential key in remaining set
    final var searchIncrement = 1 + (hash % mprime);
    var searchingPosition = (position + searchIncrement) % keySpace;

    while (searchingPosition != position) {
      final var searchingEntry = values[searchingPosition];

      // if empty, key cant be present already, assign to this slot
      if (searchingEntry == null) {
        return null;
      }

      // if equal, found key, return position
      if (searchingEntry.key().equals(key)) {
        return searchingEntry.value();
      }

      // not found, but not hit exit condition, continue searching
      searchingPosition = (searchingPosition + searchIncrement) % keySpace;
    }

    return null;
  }

  /**
   * Puts a key into the hash map.
   *
   * @param key   the key to assign the value to
   * @param value the value to insert
   * @return the old value, or null
   * @throws RuntimeException if there is no further room in the hash map for the value
   */
  public V put(final K key, final V value) {

    if (key == null) {
      return null;
    }

    final var hash = key.hashCode();
    final var position = Math.abs(hash % keySpace);
    final var currentEntryAtPosition = values[position];

    // if location is empty, or key present is equal to param, return position of the key
    if (currentEntryAtPosition == null
        || (currentEntryAtPosition != deleted && currentEntryAtPosition.key().equals(key))) {
      values[position] = new Node<>(key, value);
      return currentEntryAtPosition == null ? null : currentEntryAtPosition.value();
    }

    // collision happened, search for potential key in remaining set
    final var searchIncrement = 1 + (hash % mprime);
    var searchingPosition = (position + searchIncrement) % keySpace;
    var nextViableInsertLocation = -1;

    while (searchingPosition != position) {
      final var searchingEntry = values[searchingPosition];

      // if empty, key can't be present already, assign to this slot
      if (searchingEntry == null) {
        values[searchingPosition] = new Node<>(key, value);
        return null;
      }

      // mark the first deleted entry we find as the next viable insert location if we don't find an empty location
      if (deleted.equals(searchingEntry) && nextViableInsertLocation == -1) {
        nextViableInsertLocation = searchingPosition;
      }

      // if equal, found key, return position
      if (!deleted.equals(searchingEntry) && searchingEntry.key().equals(key)) {
        values[searchingPosition] = new Node<>(key, value);
        return searchingEntry.value();
      }

      // not found, but not hit exit condition, continue searching
      searchingPosition = (searchingPosition + searchIncrement) % keySpace;
    }

    // key does not exist in set, if current position is marked as deleted, use current position
    if (deleted.equals(currentEntryAtPosition)) {
      values[position] = new Node<>(key, value);
      return null;
    }

    // key not found, or an empty space to insert key, insert at next viable location or fail
    if (nextViableInsertLocation == -1) {
      throw new RuntimeException("hash map is full");
    }

    final var old = values[nextViableInsertLocation];
    values[nextViableInsertLocation] = new Node<>(key, value);
    return old.value();
  }

  /**
   * Deletes the value currently associated with the key.
   *
   * @param key the key to remove
   * @return the old value, or null
   */
  public V delete(final K key) {

    if (key == null) {
      return null;
    }

    final var hash = key.hashCode();
    final var position = Math.abs(hash % keySpace);

    final var currentEntryAtPosition = values[position];

    // if location is empty, key cant be present
    if (currentEntryAtPosition == null) {
      return null;
    }

    // if key is present at location, mark as deleted
    if (currentEntryAtPosition != deleted && currentEntryAtPosition.key().equals(key)) {
      values[position] = deleted;
      return currentEntryAtPosition.value();
    }

    final var searchIncrement = 1 + (hash % mprime);
    var searchingPosition = (position + searchIncrement) % keySpace;

    while (searchingPosition != position) {
      final var searchingEntry = values[searchingPosition];

      // if empty, key cant be present
      if (searchingEntry == null) {
        return null;
      }

      // if equal, found key, delete it
      if (searchingEntry != deleted && searchingEntry.key().equals(key)) {
        values[searchingPosition] = deleted;
        return searchingEntry.value();
      }

      // not found, but not hit exit condition, continue searching
      searchingPosition = (searchingPosition + searchIncrement) % keySpace;
    }

    return null;
  }
}

