package org.guardiandev.yak.server.responder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.guardiandev.yak.server.http.Constants;
import org.guardiandev.yak.server.pool.MemoryPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the http protocol over a tcp socket, sending a http response to the client.
 */
public final class HttpResponder implements Responder {

  private static final Logger LOG = LoggerFactory.getLogger(HttpResponder.class);

  private final SocketChannel rawConnection;
  private final MemoryPool<ByteBuffer> networkBufferPool;
  private final ByteBuffer writeBuffer;
  private final String requestId;

  /**
   * Creates a http responder.
   *
   * @param rawConnection     the tcp connection to send the response to
   * @param networkBufferPool the pool of network buffers to take from
   * @param requestId         the id to use when logging
   */
  public HttpResponder(final SocketChannel rawConnection,
                       final MemoryPool<ByteBuffer> networkBufferPool,
                       final String requestId) {
    this.rawConnection = rawConnection;
    this.networkBufferPool = networkBufferPool;
    this.writeBuffer = networkBufferPool.take();
    this.requestId = requestId;
  }

  /**
   * Build the http response in full, and place it in the {@link #writeBuffer} ready to be sent.
   *
   * @param responseCode the response type to build
   * @param body         the optional response body to include
   */
  @Override
  public void bufferResponse(final Result responseCode, final ByteBuffer body) {
    buildRequestLine(responseCode);
    writeBuffer.put(Constants.CRLF_SEQUENCE_BYTES);
    buildHeaders(body);
    writeBuffer.put(Constants.CRLF_SEQUENCE_BYTES);
    buildBody(body);

    writeBuffer.flip();

    LOG.debug("[requestId={},responseCode={}] buffered response for request", requestId, responseCode.getCode());
  }

  private void buildRequestLine(final Result responseCode) {
    writeBuffer.put(Constants.HTTP_VERSION_BYTES);
    writeBuffer.put(Constants.SPACE_BYTE);
    writeBuffer.put(responseCode.getCode().getBytes());
    writeBuffer.put(Constants.SPACE_BYTE);
    writeBuffer.put(Constants.SPACE_BYTE);
    writeBuffer.put(responseCode.getReasonPhrase().getBytes());
  }

  private void buildHeaders(final ByteBuffer body) {
    if (body == null || !body.hasRemaining()) {
      return;
    }

    // content length header
    writeBuffer.put(Constants.CONTENT_LENGTH_BYTES);
    writeBuffer.put(Constants.COLON_BYTE);
    writeBuffer.put(Constants.SPACE_BYTE);
    writeBuffer.put(String.valueOf(body.remaining()).getBytes());
    writeBuffer.put(Constants.CRLF_SEQUENCE_BYTES);
  }

  private void buildBody(final ByteBuffer body) {
    if (body == null || !body.hasRemaining()) {
      return;
    }

    writeBuffer.put(body);
  }

  /**
   * Write the {@link #writeBuffer} to the {@link #rawConnection}.
   *
   * @return true if finished, else false.
   * @throws IOException if unable to write to socket
   */
  @Override
  public boolean progress() throws IOException {

    LOG.trace("[requestId={}] progressing response", requestId);

    final int bytesWritten = rawConnection.write(writeBuffer);
    if (bytesWritten == -1) {
      LOG.debug("[requestId={}] failed to write to socket, making connection as done", requestId);
      return true;
    }

    final var hasCompleted = !writeBuffer.hasRemaining();

    if (hasCompleted) {
      LOG.debug("[requestId={}] finished writing response to connection", requestId);
    }

    return hasCompleted;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SocketChannel getSocket() {
    return rawConnection;
  }

  /**
   * Return the write buffer to the pool to be used again.
   */
  @Override
  public void cleanup() {
    networkBufferPool.returnToPool(writeBuffer);
  }
}
