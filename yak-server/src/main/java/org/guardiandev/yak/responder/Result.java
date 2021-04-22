package org.guardiandev.yak.responder;

public enum Result {
  KEY_NOT_FOUND("404", "Not Found Key"),
  KEY_CREATED("201", "Created Key");

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

