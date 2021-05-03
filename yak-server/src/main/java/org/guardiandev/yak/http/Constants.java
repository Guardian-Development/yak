package org.guardiandev.yak.http;

/**
 * Constants used when processing http requests.
 */
public final class Constants {

  private Constants() {
  }

  public static final byte[] HTTP_VERSION_BYTES = "HTTP/1.1".getBytes();
  public static final byte CR = "\r".getBytes()[0];
  public static final byte LF = "\n".getBytes()[0];
  public static final String COLON = ":";
  public static final byte COLON_BYTE = ":".getBytes()[0];
  public static final String SPACE = " ";
  public static final byte SPACE_BYTE = SPACE.getBytes()[0];
  public static final String QUOTE = "\"";
  public static final String SLASH = "/";
  public static final String CRLF_SEQUENCE = "\r\n";
  public static final byte[] CRLF_SEQUENCE_BYTES = CRLF_SEQUENCE.getBytes();
  public static final String CONTENT_LENGTH = "Content-Length";
  public static final byte[] CONTENT_LENGTH_BYTES = CONTENT_LENGTH.getBytes();
  public static final String HTTP_REQUEST_ID_HEADER = "x-request-id";
}
