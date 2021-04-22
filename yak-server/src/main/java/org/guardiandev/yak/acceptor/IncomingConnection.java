package org.guardiandev.yak.acceptor;

import java.io.IOException;

interface IncomingConnection {
  boolean progress() throws IOException;
  boolean hasError();
  IncomingCacheRequest getRequest();
}
