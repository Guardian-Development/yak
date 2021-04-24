package org.guardiandev.yak.responder;

import org.guardiandev.yak.http.Constants;
import org.guardiandev.yak.pool.MemoryPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public final class HttpResponder implements Responder {

  private static final Logger LOG = LoggerFactory.getLogger(HttpResponder.class);

  private final SocketChannel rawConnection;
  private final MemoryPool<ByteBuffer> networkBufferPool;
  private final ByteBuffer writeBuffer;

  public HttpResponder(final SocketChannel rawConnection, final MemoryPool<ByteBuffer> networkBufferPool) {
    this.rawConnection = rawConnection;
    this.networkBufferPool = networkBufferPool;
    this.writeBuffer = networkBufferPool.take();
  }

  @Override
  public void bufferResponse(final Result responseCode, final ByteBuffer body) {
    buildRequestLine(responseCode);
    writeBuffer.put(Constants.CRLF_SEQUENCE_BYTES);
    buildHeaders(body);
    writeBuffer.put(Constants.CRLF_SEQUENCE_BYTES);
    buildBody(body);

    writeBuffer.flip();
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

  @Override
  public boolean progress() throws IOException {

    final int bytesWritten = rawConnection.write(writeBuffer);
    if (bytesWritten == -1) {
      LOG.debug("failed to write to socket, making connection as done");
      return true;
    }

    return !writeBuffer.hasRemaining();
  }

  @Override
  public SocketChannel getSocket() {
    return rawConnection;
  }

  @Override
  public void cleanup() {
    networkBufferPool.returnToPool(writeBuffer);
  }
}
