package org.guardiandev.yak.acceptor;

import java.nio.channels.SocketChannel;

public final class IncomingConnectionFactory {

  public IncomingConnection wrapConnection(final SocketChannel rawConnection) {
    return new IncomingHttpConnection(rawConnection, new HttpRequest());
  }
}