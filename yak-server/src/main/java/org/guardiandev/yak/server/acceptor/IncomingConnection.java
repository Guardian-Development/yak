package org.guardiandev.yak.server.acceptor;

import java.io.IOException;

interface IncomingConnection {

  boolean progress() throws IOException;

  boolean hasError();

  IncomingCacheRequest getRequest();

  void cleanup();
}
