package org.guardiandev.yak.server.acceptor;

/**
 * The type of incoming request for a {@link org.guardiandev.yak.server.cacheprogression.CacheWrapper} to handle.
 */
public enum IncomingCacheRequestType {
  GET,
  CREATE,
  NOT_SUPPORTED
}
