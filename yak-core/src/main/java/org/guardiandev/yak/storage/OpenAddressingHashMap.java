package org.guardiandev.yak.storage;

import java.util.UUID;
import org.guardiandev.yak.utils.IntegerExtensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a hash map of key to integer, where the integer is equal to the index of the key in the map.
 *
 * @param <K> the type of the key.
 */
public final class OpenAddressingHashMap<K> {

  private static final Logger LOG = LoggerFactory.getLogger(OpenAddressingHashMap.class);

  private static final Object DELETED = new Object();

  private final String name;
  private final Object[] keys;

  // m and m', where m` = m - 1
  // h1(k) = k mod m = initial location
  // h2(k) = 1 + (k mod m') = increment between locations
  private final int keySpace;
  private final int mprime;

  /**
   * Creates a hash map of the fixedSize using the open addressing collision resolution technique.
   *
   * <p>
   * The fixed size must be a power of 2, as this allows for better resolutions of collisions as we use
   * Double Hashing when probing the table.
   * </p>
   *
   * @param fixedSize the size of the hash map
   */
  public OpenAddressingHashMap(final int fixedSize) {
    assert IntegerExtensions.isPowerOf2(fixedSize) : "the size of the hashmap must be a power of 2";

    this.keys = new Object[fixedSize];
    this.mprime = fixedSize - 1;
    this.keySpace = keys.length;

    this.name = String.format("storage-%s", UUID.randomUUID().toString().substring(0, 5));

    LOG.debug("[{}] hash map created with fixed size {}", name, fixedSize);
  }

  // TODO: need a get existing method for the get, where we dont want it to assign a key if not present

  /**
   * Assigns the key to a fixed location in the hash map.
   *
   * <p>
   * uses the {@link #hashCode()} of the key % fixedSize to work out location.
   * if the current entry at that location is null, assign the key to the location.
   * if the current entry at that location is {@link #equals(Object)} to the key, assign the key to the location.
   * if neither of these are true, we do a linear probe throughout the {@link #keys} to see if the key exists at
   * a different location.
   * </p>
   * <p>
   * This works by starting at the location we expected the key and moving forward until we hit either:
   * an entry that is {@link #equals(Object)} to the key, then we assign the key to the existing location.
   * Or, we hit an empty element, then we know the key must not exist in the set currently, and assign it to the
   * empty location.
   * If neither of these happen, and we search the entire {@link #keys} then we know the key must not exist. If we
   * have encountered a deleted entry within that search, we assign the key to the first deleted entry.
   * If no deleted entries have been found, we have a full {@link #keys} and return null.
   * </p>
   *
   * @param key the key you wish to get an assignment for
   * @return the assignment, or null if the {@link #keys} is full and the key does not currently exist
   */
  public Integer getExistingOrAssign(final K key) {
    LOG.trace("[name={},key={}] getting position for key", name, key);

    if (key == null) {
      LOG.trace("[name={},key={}] null key not stored, returning null", name, key);
      return null;
    }

    final var hash = key.hashCode();
    final var position = Math.abs(hash % keySpace);

    final var currentEntryAtPosition = keys[position];

    // if location is empty, or key present is equal to param, return position of the key
    if (currentEntryAtPosition == null || currentEntryAtPosition.equals(key)) {
      LOG.trace("[name={},key={}] initial position empty or equal to key, assigning position {} to key", name, key, position);
      keys[position] = key;
      return position;
    }

    // collision happened, search for potential key in remaining set
    final var searchIncrement = 1 + (hash % mprime);
    var searchingPosition = (position + searchIncrement) % keySpace;
    var nextViableInsertLocation = -1;

    LOG.trace("[name={},key={}] initial position {} not equal to key, searching for key with increment {}", name, key, position, searchIncrement);

    while (searchingPosition != position) {
      final var searchingEntry = keys[searchingPosition];

      LOG.trace("[name={},key={}] searching position {}", name, key, searchingPosition);

      // if empty, key cant be present already, assign to this slot
      if (searchingEntry == null) {
        LOG.trace("[name={},key={}] position {} empty, assigning position to key", name, key, searchingPosition);
        keys[searchingPosition] = key;
        return searchingPosition;
      }

      // mark the first deleted entry we find as the next viable insert location if we don't find an empty location
      if (DELETED.equals(searchingEntry) && nextViableInsertLocation == -1) {
        nextViableInsertLocation = searchingPosition;
      }

      // if equal, found key, return position
      if (searchingEntry.equals(key)) {
        LOG.trace("[name={},key={}] position {} contains existing key of same value, assigning position to key", name, key, searchingPosition);

        keys[searchingPosition] = key;
        return searchingPosition;
      }

      // not found, but not hit exit condition, continue searching
      searchingPosition = (searchingPosition + searchIncrement) % keySpace;
      LOG.trace("[name={},key={}] stopping condition not met, continuing search", name, key);
    }

    // key does not exist in set, if current position is marked as deleted, use current position
    if (DELETED.equals(currentEntryAtPosition)) {
      LOG.trace("[name={},key={}] key did not exists in set, current position {} deleted, assigning key", name, key, position);
      keys[position] = key;
      return position;
    }

    // key not found, or an empty space to insert key, insert at next viable location or fail
    if (nextViableInsertLocation == -1) {
      LOG.trace("[name={},key={}] key not found and no deleted entries in set, hash map is full", name, key);
      return null;
    }

    keys[nextViableInsertLocation] = key;

    LOG.trace("[name={},key={}] position {} contained deleted entry, assigning position to key", name, key, nextViableInsertLocation);

    return nextViableInsertLocation;
  }

  /**
   * Ensures key is no longer present in the hash map.
   *
   * <p>
   * uses the {@link #hashCode()} of the key % fixedSize to work out location.
   * if the current entry at that location is null, does nothing, return true.
   * if the current entry at that location is {@link #equals(Object)} to the key, marks key as deleted, return true.
   * if neither of these are true, we do a linear probe throughout the {@link #keys} to see if the key exists at
   * a different location.
   * </p>
   * <p>
   * This works by starting at the location we expected the key and moving forward until we hit either:
   * an entry that is {@link #equals(Object)} to the key, then we mark the key as deleted and return true.
   * Or, we hit an empty element, then we know the key must not exist in the set currently, and return true.
   * If neither of these happen, then the key cannot exist, and we return true.
   * </p>
   *
   * @param key the key to ensure is deleted
   * @return true if the key is no longer present in the hash map.
   */
  public boolean delete(final K key) {
    LOG.trace("[name={},key={}] deleting key", name, key);

    if (key == null) {
      LOG.trace("[name={},key={}] null key not stored, returning false", name, key);
      return false;
    }

    final var hash = key.hashCode();
    final var position = Math.abs(hash % keySpace);

    final var currentEntryAtPosition = keys[position];

    // if location is empty, key cant be present
    if (currentEntryAtPosition == null) {
      LOG.trace("[name={},key={}] initial position {} empty, key does not exist in set", name, key, position);
      return true;
    }

    // if key is present at location, mark as deleted
    if (currentEntryAtPosition.equals(key)) {
      LOG.trace("[name={},key={}] initial position {} equal to key, marking position as deleted", name, key, position);
      keys[position] = DELETED;
      return true;
    }

    final var searchIncrement = 1 + (hash % mprime);
    var searchingPosition = (position + searchIncrement) % keySpace;

    LOG.trace("[name={},key={}] initial position {} not equal to key, searching for key with increment {}", name, key, position, searchIncrement);

    while (searchingPosition != position) {
      final var searchingEntry = keys[searchingPosition];

      LOG.trace("[name={},key={}] searching position {}", name, key, searchingPosition);

      // if empty, key cant be present
      if (searchingEntry == null) {
        LOG.trace("[name={},key={}] position {} is empty, key does not exist in set", name, key, searchingPosition);
        return true;
      }

      // if equal, found key, delete it
      if (searchingEntry.equals(key)) {
        LOG.trace("[name={},key={}] position {} equal to key, marking position as deleted", name, key, searchingPosition);
        keys[searchingPosition] = DELETED;
        return true;
      }

      // not found, but not hit exit condition, continue searching
      searchingPosition = (searchingPosition + searchIncrement) % keySpace;
      LOG.trace("[name={},key={}] stopping condition not met, continuing search", name, key);
    }

    LOG.trace("[name={},key={}] key not found in set", name, key);

    return true;
  }

  @Override
  public String toString() {
    return "OpenAddressingHashMap{" + "storageName='" + name + '\'' + '}';
  }
}
