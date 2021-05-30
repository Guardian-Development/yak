package org.guardiandevelopment.yak.server.acceptor;

import java.io.IOException;

interface IncomingConnection {

  boolean progress() throws IOException;

  boolean hasError();

  HttpRequest getRequest();

  void cleanup();
}
