package org.guardiandev.yak.acceptor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public final class IncomingHttpConnection implements IncomingConnection {

  private enum ProcessingStage {
    REQUEST_URI,
    HEADERS,
    MESSAGE_BODY
  }

  private final static byte CR = "\r".getBytes()[0];
  private final static byte LF = "\n".getBytes()[0];
  private final static String SPACE = " ";
  private final static String SLASH = "/";
  private final static String CRLF_SEQUENCE = "\r\n";

  private final SocketChannel rawConnection;
  private final HttpRequest request;
  private boolean isComplete;

  private final ByteBuffer readBuffer;
  private int processedPosition;
  private int processedCommittedPosition;

  private ProcessingStage stage;

  public IncomingHttpConnection(final SocketChannel rawConnection, final HttpRequest request) {
    this.rawConnection = rawConnection;
    this.request = request;
    this.isComplete = false;
    // TODO: memory pool read buffers
    this.readBuffer = ByteBuffer.allocate(512);
    this.stage = ProcessingStage.REQUEST_URI;
    this.processedPosition = 0;
    this.processedCommittedPosition = 0;

    request.reset();
  }

  @Override
  public boolean progress() throws IOException {

    // TODO: handle -1 and close
    final var bytesRead = rawConnection.read(readBuffer);
    if (bytesRead == 0) {
      return false;
    }

    // mark position in buffer you have used for reading
    final var readPosition = readBuffer.position();
    readBuffer.position(processedPosition);

    // process any new bytes
    var previousByte = 0x00;
    while(readBuffer.position() < readPosition) {
      final var nextByte = readBuffer.get();

      if (previousByte == CR && nextByte == LF) {
        switch (stage) {
          // TODO: handle body and header variables, store as bytes
          case REQUEST_URI:
            final var endPosition = readBuffer.position();
            extractRequestUri(processedCommittedPosition, endPosition - 2);
            stage = ProcessingStage.HEADERS;
            // TODO: can compact buffer and remove bytes before commited position now
            processedCommittedPosition = endPosition;
            isComplete = true;
            break;
        }
      }

      previousByte = nextByte;
    }

    // set variables ready for next read cycle
    processedPosition = readBuffer.position();
    readBuffer.position(readPosition);

    return isComplete;
  }

  private void extractRequestUri(final int startingPosition, final int endPosition) {
    final var stringBuffer = new StringBuilder(endPosition - startingPosition);
    readBuffer.position(startingPosition);
    while(readBuffer.position() < endPosition) {
      stringBuffer.append((char)readBuffer.get());
    }

    final var requestUriParts = stringBuffer.toString().split(SPACE);
    request.setMethod(requestUriParts[0]);
    request.setRequestUri(requestUriParts[1]);
    request.setHttpVersion(requestUriParts[2]);
  }

  @Override
  public IncomingCacheRequest getRequest() {
    assert isComplete : "can not get request for incomplete incoming connection";

    final var uriParts = request.getRequestUri().split(SLASH);
    final var key = uriParts[uriParts.length - 1];
    final var cache = uriParts[uriParts.length - 2];

    return new IncomingCacheRequest(cache, key, rawConnection);
  }
}
