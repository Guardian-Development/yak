package org.guardiandev.yak.acceptor;

import org.guardiandev.yak.pool.MemoryPool;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public final class IncomingConnectionFactory {

  private final MemoryPool<ByteBuffer> networkBufferPool;
  private final MemoryPool<HttpRequest> httpRequestMemoryPool;
  private final MemoryPool<IncomingCacheRequest> incomingCacheRequestMemoryPool;

  public IncomingConnectionFactory(final MemoryPool<ByteBuffer> networkBufferPool,
                                   final MemoryPool<HttpRequest> httpRequestMemoryPool,
                                   final MemoryPool<IncomingCacheRequest> incomingCacheRequestMemoryPool) {

    this.networkBufferPool = networkBufferPool;
    this.httpRequestMemoryPool = httpRequestMemoryPool;
    this.incomingCacheRequestMemoryPool = incomingCacheRequestMemoryPool;
  }

  public IncomingConnection wrapConnection(final SocketChannel rawConnection) {
    return new IncomingHttpConnection(
            rawConnection, networkBufferPool, httpRequestMemoryPool, incomingCacheRequestMemoryPool);
  }
}