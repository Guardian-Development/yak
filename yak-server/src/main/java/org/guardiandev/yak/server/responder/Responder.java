package org.guardiandev.yak.server.responder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Represents a responder that is able to build and send responses to connections.
 */
public interface Responder {

  /**
   * Build the response in full, and buffer it ready for sending.
   *
   * @param result the response type to build
   * @param body   the optional response body to include
   */
  void bufferResponse(final Result result, final ByteBuffer body);

  /**
   * Send the response.
   * <p>
   * must have called {@link #bufferResponse(Result, ByteBuffer)} before beginning sending.
   * </p>
   *
   * @return true if sending is finished, else false
   * @throws IOException if an error occurs during sending
   */
  boolean progress() throws IOException;

  /**
   * Get the underlying tcp connection.
   *
   * @return the underlying tcp connection this responder sends its response to
   */
  SocketChannel getSocket();

  /**
   * cleanup any resources this responder is using.
   * <p>
   * this must be called before this responder goes out of scope, otherwise resources may be held
   * unnecessarily.
   * </p>
   */
  void cleanup();
}
