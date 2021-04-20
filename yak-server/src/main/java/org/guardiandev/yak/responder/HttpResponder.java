package org.guardiandev.yak.responder;

import org.guardiandev.yak.http.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public final class HttpResponder implements Responder {

  private static final Logger LOG = LoggerFactory.getLogger(HttpResponder.class);

  private final SocketChannel rawConnection;
  private final ByteBuffer writeBuffer;

  // TODO: have a responder factory, attach to initial request, then once cache is done it can pull that off and pass
  //  variables needed to use this, then we can run multiple protocols at same time
  public HttpResponder(final SocketChannel rawConnection) {
    this.rawConnection = rawConnection;
    this.writeBuffer = ByteBuffer.allocate(512);
  }

  @Override
  public void bufferResponse(final Result responseCode, final ByteBuffer body) {
    writeBuffer.put(Constants.HTTP_VERSION_BYTES);
    writeBuffer.put(Constants.SPACE_BYTE);
    writeBuffer.put(responseCode.getCode().getBytes());
    writeBuffer.put(Constants.SPACE_BYTE);
    writeBuffer.put(Constants.SPACE_BYTE);
    writeBuffer.put(responseCode.getReasonPhrase().getBytes());
    writeBuffer.put(Constants.CRLF_SEQUENCE_BYTES);

    // todo: this gap is where the body would go
    writeBuffer.put(Constants.CRLF_SEQUENCE_BYTES);

    writeBuffer.flip();
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
}
