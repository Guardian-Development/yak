package org.guardiandev.yak.pool;

import org.guardiandev.yak.acceptor.HttpRequest;
import org.guardiandev.yak.acceptor.IncomingCacheRequest;

import java.nio.ByteBuffer;

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
