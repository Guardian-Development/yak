package org.guardiandev.yak.http;

public final class Constants {

  private Constants() {
  }

  public static final byte[] HTTP_VERSION_BYTES = "HTTP/1.1".getBytes();
  public final static byte CR = "\r".getBytes()[0];
  public final static byte LF = "\n".getBytes()[0];
  public final static String SPACE = " ";
  public final static byte SPACE_BYTE = SPACE.getBytes()[0];
  public final static String SLASH = "/";
  public final static String CRLF_SEQUENCE = "\r\n";
  public final static byte[] CRLF_SEQUENCE_BYTES = CRLF_SEQUENCE.getBytes();
}
