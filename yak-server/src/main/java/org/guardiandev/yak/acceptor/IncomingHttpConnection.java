package org.guardiandev.yak.acceptor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.guardiandev.yak.http.Constants;
import org.guardiandev.yak.pool.MemoryPool;
import org.guardiandev.yak.responder.HttpResponder;

/**
 * Provides the http protocol over a tcp socket, reading an incoming request.
 */
public final class IncomingHttpConnection implements IncomingConnection {

  private enum ProcessingStage {
    REQUEST_URI,
    HEADERS,
    MESSAGE_BODY
  }

  private final SocketChannel rawConnection;
  private final MemoryPool<ByteBuffer> networkBufferPool;
  private final MemoryPool<HttpRequest> httpRequestMemoryPool;
  private final ByteBuffer readBuffer;
  private final HttpRequest request;
  private final MemoryPool<IncomingCacheRequest> incomingCacheRequestMemoryPool;

  private boolean isComplete;
  private boolean hasError;
  private int processedPosition;
  private int processedCommittedPosition;

  private ProcessingStage stage;

  /**
   * Creates a new incoming http connection, taking resources off the memory pools provided on creation.
   *
   * @param rawConnection                  the raw tcp connection
   * @param networkBufferPool              the pool to take a network byte buffer from
   * @param httpRequestMemoryPool          the pool to take a http request from
   * @param incomingCacheRequestMemoryPool the pool to take an incoming cache request from
   */
  public IncomingHttpConnection(final SocketChannel rawConnection,
                                final MemoryPool<ByteBuffer> networkBufferPool,
                                final MemoryPool<HttpRequest> httpRequestMemoryPool,
                                final MemoryPool<IncomingCacheRequest> incomingCacheRequestMemoryPool) {
    this.rawConnection = rawConnection;
    this.networkBufferPool = networkBufferPool;
    this.httpRequestMemoryPool = httpRequestMemoryPool;
    this.request = httpRequestMemoryPool.take();
    this.incomingCacheRequestMemoryPool = incomingCacheRequestMemoryPool;
    this.isComplete = false;
    this.readBuffer = networkBufferPool.take();
    this.stage = ProcessingStage.REQUEST_URI;
    this.processedPosition = 0;
    this.processedCommittedPosition = 0;

    readBuffer.clear();
    request.reset();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean progress() throws IOException {

    final var bytesRead = rawConnection.read(readBuffer);

    if (bytesRead == 0) {
      return false;
    }

    if (bytesRead == -1) {
      hasError = true;
      return true;
    }

    // mark position in buffer you have used for reading
    final var readPosition = readBuffer.position();
    readBuffer.position(processedPosition);

    if (stage == ProcessingStage.MESSAGE_BODY) {
      final var bodySizeBuffered = readPosition - processedCommittedPosition;
      final var messageBodyLength = extractMessageBodyLength();
      if (bodySizeBuffered >= messageBodyLength) {
        extractMessageBody(processedCommittedPosition, messageBodyLength);
        isComplete = true;
        return true;
      }
    } else {
      // process any new bytes
      var previousByte = readBuffer.position() > 0 ? readBuffer.get(readBuffer.position() - 1) : 0x00;
      while (readBuffer.position() < readPosition) {
        final var thisByte = readBuffer.get();

        if (previousByte == Constants.CR && thisByte == Constants.LF) {
          if (stage == ProcessingStage.REQUEST_URI) {
            final var requestUriEndPosition = readBuffer.position();
            extractRequestUri(processedCommittedPosition, requestUriEndPosition - 2);
            readBuffer.position(requestUriEndPosition);
            stage = ProcessingStage.HEADERS;
            processedCommittedPosition = requestUriEndPosition;
          } else if (stage == ProcessingStage.HEADERS) {
            final var headersEndPosition = readBuffer.position();
            if (headersComplete(processedCommittedPosition, headersEndPosition - 2)) {
              stage = ProcessingStage.MESSAGE_BODY;
              readBuffer.position(headersEndPosition);
              processedCommittedPosition = headersEndPosition;
              final var bodyLength = extractMessageBodyLength();
              if (bodyLength == 0) {
                isComplete = true;
                return true;
              }
            } else {
              extractHeader(processedCommittedPosition, headersEndPosition - 2);
              readBuffer.position(headersEndPosition);
              processedCommittedPosition = headersEndPosition;
            }
          }
        } else {
          previousByte = thisByte;
        }
      }
    }

    // set variables ready for next read cycle
    processedPosition = readBuffer.position();
    readBuffer.position(readPosition);

    return false;
  }

  private void extractRequestUri(final int startingPosition, final int endPosition) {
    final var stringBuffer = new StringBuilder(endPosition - startingPosition);
    readBuffer.position(startingPosition);
    while (readBuffer.position() < endPosition) {
      stringBuffer.append((char) readBuffer.get());
    }

    final var requestUriParts = stringBuffer.toString().split(Constants.SPACE);
    request.setMethod(requestUriParts[0]);
    request.setRequestUri(requestUriParts[1]);
    request.setHttpVersion(requestUriParts[2]);
  }

  private void extractHeader(final int startingPosition, final int endPosition) {
    final var stringBuffer = new StringBuilder(endPosition - startingPosition);
    readBuffer.position(startingPosition);
    while (readBuffer.position() < endPosition) {
      stringBuffer.append((char) readBuffer.get());
    }

    final var header = stringBuffer.toString();
    final var colonIndex = header.indexOf(Constants.COLON);

    final var key = header.substring(0, colonIndex).toLowerCase();
    final var value = header.substring(colonIndex + 1).stripLeading();

    request.addHeader(key, value);
  }

  private boolean headersComplete(final int startingPosition, final int endPosition) {
    return startingPosition == endPosition;
  }

  private int extractMessageBodyLength() {
    final var length = request.getHeaderOrNull(Constants.CONTENT_LENGTH.toLowerCase());
    if (length == null) {
      return 0;
    }

    return Integer.parseInt(length);
  }

  private void extractMessageBody(final int startingPosition, final int length) {
    request.setBodyStartIndex(startingPosition);
    request.setBodyLength(length);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasError() {
    return hasError;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IncomingCacheRequest getRequest() {
    assert isComplete : "can not get request for incomplete incoming connection";

    final var uriParts = request.getRequestUri().split(Constants.SLASH);
    final var key = uriParts[uriParts.length - 1];
    final var cache = uriParts[uriParts.length - 2];
    final var type = typeFromMethod(request.getMethod());

    readBuffer.position(request.getBodyStartIndex());
    readBuffer.limit(readBuffer.position() + request.getBodyLength());

    final var request = incomingCacheRequestMemoryPool.take();
    request.reset();

    return request
            .setResponder(new HttpResponder(rawConnection, networkBufferPool))
            .setCacheName(cache)
            .setKeyName(key)
            .setType(type)
            .setContent(readBuffer);
  }

  /**
   * Returns any resources we used to their respective memory pools.
   */
  @Override
  public void cleanup() {
    networkBufferPool.returnToPool(readBuffer);
    httpRequestMemoryPool.returnToPool(request);
  }

  private IncomingCacheRequestType typeFromMethod(final String httpMethod) {
    if ("GET".equalsIgnoreCase(httpMethod)) {
      return IncomingCacheRequestType.GET;
    }

    if ("POST".equalsIgnoreCase(httpMethod)) {
      return IncomingCacheRequestType.CREATE;
    }

    return IncomingCacheRequestType.NOT_SUPPORTED;
  }
}
