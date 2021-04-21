package org.guardiandev.yak.acceptor;

import java.nio.ByteBuffer;
import java.util.HashMap;

public final class HttpRequest {
  private String method;
  private String requestUri;
  private String httpVersion;
  private final HashMap<String, String> headers = new HashMap<>();
  // TODO: memory pool
  private final ByteBuffer body = ByteBuffer.allocate(512);

  public String getMethod() {
    return method;
  }

  public HttpRequest setMethod(String method) {
    this.method = method;
    return this;
  }

  public String getRequestUri() {
    return requestUri;
  }

  public HttpRequest setRequestUri(String requestUri) {
    this.requestUri = requestUri;
    return this;
  }

  public String getHttpVersion() {
    return httpVersion;
  }

  public HttpRequest setHttpVersion(String httpVersion) {
    this.httpVersion = httpVersion;
    return this;
  }

  public HttpRequest addHeader(final String key, final String value) {
    headers.put(key, value);
    return this;
  }

  public String getHeaderOrNull(final String key) {
    return headers.get(key);
  }

  public HttpRequest saveBody(final ByteBuffer buffer, final int length) {
    final var previousLimit = buffer.limit();

    buffer.limit(buffer.position() + length);
    body.put(buffer);
    body.flip();

    buffer.limit(previousLimit);
    return this;
  }

  public HttpRequest reset() {
    this.method = null;
    this.requestUri = null;
    this.httpVersion = null;
    this.headers.clear();
    this.body.clear();
    return this;
  }
}
