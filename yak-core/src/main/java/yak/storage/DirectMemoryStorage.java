package yak.storage;

import java.nio.ByteBuffer;
import org.agrona.concurrent.UnsafeBuffer;

/**
 * Provides storage via a pre-allocated Direct {@link ByteBuffer}, allocated at {@link #init} of the cache lifecycle.
 *
 * <p>
 * This storage mechanism should be used where the maximumKeys * fixedValueSize fits into RAM.
 * </p>
 */
public final class DirectMemoryStorage implements YakValueStorage {

  private ByteBuffer inMemoryStorage;
  private UnsafeBuffer[] buffers;

  /**
   * {@inheritDoc}
   */
  @Override
  public void init(final int maximumKeys, final int fixedValueSize) {
    final var bufferSize = maximumKeys * fixedValueSize;
    inMemoryStorage = ByteBuffer.allocateDirect(bufferSize);
    buffers = new UnsafeBuffer[maximumKeys];
    for (int i = 0; i < maximumKeys; i++) {
      final var offset = fixedValueSize * i;
      // create a buffer which is limited to the index in the storage
      buffers[i] = new UnsafeBuffer(inMemoryStorage, offset, fixedValueSize);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public UnsafeBuffer getStorage(int index) {
    if (index < 0 || index >= buffers.length) {
      throw new IllegalArgumentException("the index should be between 0 and maximumKeys");
    }

    return buffers[index];
  }
}
