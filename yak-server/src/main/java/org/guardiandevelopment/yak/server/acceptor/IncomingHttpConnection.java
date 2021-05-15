package org.guardiandevelopment.yak.server.acceptor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.guardiandevelopment.yak.server.http.Constants;
import org.guardiandevelopment.yak.server.pool.MemoryPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the http protocol over a tcp socket, reading an incoming request.
 */
public final class IncomingHttpConnection implements IncomingConnection {

  private static final Logger LOG = LoggerFactory.getLogger(IncomingHttpConnection.class);

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
  private final String correlationId;

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
   * @param correlationId                  this is used for log correlation
   */
  public IncomingHttpConnection(final SocketChannel rawConnection,
                                final MemoryPool<ByteBuffer> networkBufferPool,
                                final MemoryPool<HttpRequest> httpRequestMemoryPool,
                                final String correlationId) {
    this.rawConnection = rawConnection;
    this.networkBufferPool = networkBufferPool;
    this.httpRequestMemoryPool = httpRequestMemoryPool;
    this.request = httpRequestMemoryPool.take();
    this.correlationId = correlationId;
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

    if (isComplete) {
      return true;
    }

    LOG.trace("[correlationId={},request={}] progressing request", correlationId, request);

    final var bytesRead = rawConnection.read(readBuffer);

    if (bytesRead == -1) {
      hasError = true;
      return true;
    }

    // mark position in buffer you have used for reading
    final var readPosition = readBuffer.position();
    readBuffer.position(processedPosition);

    if (stage == ProcessingStage.REQUEST_URI || stage == ProcessingStage.HEADERS) {
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

            LOG.trace("[correlationId={},request={}] request uri extracted", correlationId, request);

          } else if (stage == ProcessingStage.HEADERS) {

            final var headersEndPosition = readBuffer.position();
            if (headersComplete(processedCommittedPosition, headersEndPosition - 2)) {
              stage = ProcessingStage.MESSAGE_BODY;
              readBuffer.position(headersEndPosition);
              processedCommittedPosition = headersEndPosition;

              LOG.trace("[correlationId={},request={}] all headers extracted", correlationId, request);

              final var bodyLength = extractMessageBodyLength();
              if (bodyLength == 0) {
                LOG.debug("[correlationId={},request={}] completed processing incoming request", correlationId, request);
                isComplete = true;
                return true;
              }

            } else {
              extractHeader(processedCommittedPosition, headersEndPosition - 2);
              readBuffer.position(headersEndPosition);
              processedCommittedPosition = headersEndPosition;

              LOG.trace("[correlationId={},request={}] header extracted", correlationId, request);
            }
          }
        }

        previousByte = thisByte;
      }
    }

    if (stage == ProcessingStage.MESSAGE_BODY) {
      final var bodySizeBuffered = readPosition - processedCommittedPosition;
      final var messageBodyLength = extractMessageBodyLength();
      if (bodySizeBuffered >= messageBodyLength) {
        extractMessageBody(processedCommittedPosition, messageBodyLength);

        LOG.debug("[correlationId={},request={}] completed processing incoming request", correlationId, request);

        isComplete = true;
        return true;
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
    final var tempBuffer = readBuffer.asReadOnlyBuffer();
    tempBuffer.position(startingPosition);
    tempBuffer.limit(startingPosition + length);
    request.copyIntoRequestBody(tempBuffer);
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
  public HttpRequest getRequest() {
    assert isComplete : "can not get request for incomplete incoming connection";

    return request;
  }

  /**
   * Returns any resources we used to their respective memory pools.
   */
  @Override
  public void cleanup() {
    networkBufferPool.returnToPool(readBuffer);
    httpRequestMemoryPool.returnToPool(request);
  }
}
