package org.guardiandev.yak.acceptor;

public final class HttpRequest {
  private String method;
  private String requestUri;
  private String httpVersion;

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

  public HttpRequest reset() {
    this.method = null;
    this.requestUri = null;
    this.httpVersion = null;
    return this;
  }
}
