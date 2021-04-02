package org.guardiandev.yak.serialization;

import org.agrona.concurrent.UnsafeBuffer;

/**
 * Provides a mechanism for serializing values to, and from, an {@link UnsafeBuffer}.
 *
 * @param <Q> the value type to serialize
 */
public interface YakValueSerializer<Q> {

  /**
   * Serializes the value to the value buffer, returning the number of bytes written to the buffer.
   *
   * @param value       the value to serialize
   * @param valueBuffer the buffer to serialize in to
   * @return the number of bytes written to the buffer
   */
  int serialize(Q value, UnsafeBuffer valueBuffer);

  /**
   * Deserialize a value from the buffer.
   *
   * @param valueBuffer the buffer containing the value serialized using {@link #serialize}
   * @return the deserialized value from the buffer
   */
  Q deserialize(UnsafeBuffer valueBuffer);
}
