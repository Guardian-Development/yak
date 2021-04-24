package org.guardiandev.yak.responder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface Responder {

  void bufferResponse(final Result result, final ByteBuffer body);
  boolean progress() throws IOException;
  SocketChannel getSocket();
  void cleanup();
}
