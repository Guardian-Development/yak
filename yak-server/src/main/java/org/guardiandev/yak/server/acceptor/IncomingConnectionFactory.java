package org.guardiandev.yak.server.acceptor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.guardiandev.yak.server.pool.MemoryPool;

/**
 * Takes a raw connection, and wraps it in an {@link IncomingConnection} which will handle the connection protocol.
 */
public final class IncomingConnectionFactory {

  private final MemoryPool<ByteBuffer> networkBufferPool;
  private final MemoryPool<HttpRequest> httpRequestMemoryPool;
  private final MemoryPool<IncomingCacheRequest> incomingCacheRequestMemoryPool;

  /**
   * Creates a new connection factory.
   *
   * @param networkBufferPool              the pool of byte buffers to use when creating connections
   * @param httpRequestMemoryPool          the pool of http request objects to use when creating connections
   * @param incomingCacheRequestMemoryPool the pool of cache request objects to use when processing connections
   */
  public IncomingConnectionFactory(final MemoryPool<ByteBuffer> networkBufferPool,
                                   final MemoryPool<HttpRequest> httpRequestMemoryPool,
                                   final MemoryPool<IncomingCacheRequest> incomingCacheRequestMemoryPool) {

    this.networkBufferPool = networkBufferPool;
    this.httpRequestMemoryPool = httpRequestMemoryPool;
    this.incomingCacheRequestMemoryPool = incomingCacheRequestMemoryPool;
  }

  /**
   * Wraps a raw tcp connection with an {@link IncomingConnection} protocol.
   *
   * @param rawConnection the raw tcp connection
   * @return a wrapped connection that handles the expected protocol and reads the request off the wire
   * @throws IOException if unable to get the remote address for correlation logging
   */
  public IncomingConnection wrapConnection(final SocketChannel rawConnection) throws IOException {
    return new IncomingHttpConnection(
            rawConnection, networkBufferPool, httpRequestMemoryPool, incomingCacheRequestMemoryPool, rawConnection.getRemoteAddress().toString());
  }
}