package org.guardiandev.yak.server.pool;

import java.nio.ByteBuffer;
import org.guardiandev.yak.server.acceptor.HttpRequest;
import org.guardiandev.yak.server.acceptor.IncomingCacheRequest;

/**
 * A factory which contains factory methods for every memory pool within the server.
 */
public final class Factory {

  private Factory() {
  }

  public static MemoryPool<ByteBuffer> networkBufferPool(
          final int poolSize, final int bufferSize, final boolean fillOnCreation) {
    return new MemoryPool<>(() -> ByteBuffer.allocateDirect(bufferSize), poolSize, fillOnCreation);
  }

  public static MemoryPool<HttpRequest> httpRequestPool(
          final int poolSize, final boolean fillOnCreation) {
    return new MemoryPool<>(HttpRequest::new, poolSize, fillOnCreation);
  }

  public static MemoryPool<IncomingCacheRequest> incomingCacheRequestPool(
          final int poolSize, final int bufferSize, final boolean fillOnCreation) {
    return new MemoryPool<>(() -> new IncomingCacheRequest(bufferSize), poolSize, fillOnCreation);
  }
}
