package org.guardiandevelopment.yak.server.responder;

/**
 * Represents the response type, code, and reason to a request.
 */
public enum Result {
  KEY_FOUND("200", "Key Found"),
  KEY_NOT_FOUND("404", "Not Found Key"),
  CACHE_NOT_FOUND("404", "Not Found Cache"),
  KEY_CREATED("201", "Created Key"),
  INTERNAL_ERROR("500", "Internal Server Error");

  private final String code;
  private final String reasonPhrase;

  Result(final String code, final String reasonPhrase) {
    this.code = code;
    this.reasonPhrase = reasonPhrase;
  }

  public String getCode() {
    return code;
  }

  public String getReasonPhrase() {
    return reasonPhrase;
  }
}

