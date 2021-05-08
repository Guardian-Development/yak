package org.guardiandev.yak.core.utils;

/**
 * Static methods used to extend behaviour on the integer primitive type.
 */
public final class IntegerExtensions {

  private IntegerExtensions() {
  }

  /**
   * Returns true if the value is a power of 2, else false.
   *
   * @param value the value to test
   * @return true if the value is a power of 2, else false.
   */
  public static boolean isPowerOf2(final int value) {
    return Integer.bitCount(value) == 1;
  }
}
