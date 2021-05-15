package org.guardiandevelopment.yak.server.acceptor;

import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Used for storing all information obtained when processing an incoming http request.
 * <p>
 *   built with {@link IncomingHttpConnection}
 * </p>
 */
public final class HttpRequest {

  private String method;
  private String requestUri;
  private String httpVersion;
  private final HashMap<String, String> headers = new HashMap<>();
  private final ByteBuffer requestBody;

  public HttpRequest(final int bufferSize) {
    this.requestBody = ByteBuffer.allocateDirect(bufferSize);
  }

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

  public HttpRequest copyIntoRequestBody(final ByteBuffer content) {
    requestBody.put(content);
    requestBody.flip();
    return this;
  }

  public ByteBuffer getRequestBody() {
    return requestBody;
  }

  /**
   * Reset the object ready for it to be used again in a pool.
   *
   * @return the same object ready for reuse
   */
  public HttpRequest reset() {
    this.method = null;
    this.requestUri = null;
    this.httpVersion = null;
    this.headers.clear();
    this.requestBody.clear();
    return this;
  }

  @Override
  public String toString() {
    return "HttpRequest{" +
            "method='" + method + '\'' +
            ", requestUri='" + requestUri + '\'' +
            ", httpVersion='" + httpVersion + '\'' +
            ", headers=" + headers +
            ", requestBody=" + requestBody +
            '}';
  }
}
