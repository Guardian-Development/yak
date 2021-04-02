package org.guardiandev.yak.reference;

import org.agrona.concurrent.UnsafeBuffer;
import org.guardiandev.yak.serialization.YakValueSerializer;

/**
 * Test serializer for storing/retrieving {@link TestCacheValue} from the cache.
 */
public final class TestCacheValueSerializer implements YakValueSerializer<TestCacheValue> {

  public static final int MAXIMUM_SUPPORTED_SIZE_BYTES = 32;

  @Override
  public int serialize(final TestCacheValue value, final UnsafeBuffer valueBuffer) {
    var bytesWritten = 0;

    valueBuffer.putInt(bytesWritten, value.id);
    bytesWritten += Integer.BYTES;

    bytesWritten += valueBuffer.putStringAscii(bytesWritten, value.field1);
    return bytesWritten;
  }

  @Override
  public TestCacheValue deserialize(final UnsafeBuffer valueBuffer) {
    final var value = new TestCacheValue();

    var bytesRead = 0;

    value.id = valueBuffer.getInt(bytesRead);
    bytesRead += Integer.BYTES;

    final var stringLength = valueBuffer.getInt(bytesRead);
    value.field1 = valueBuffer.getStringAscii(bytesRead, stringLength);
    bytesRead += Integer.BYTES + stringLength;

    return value;
  }
}
