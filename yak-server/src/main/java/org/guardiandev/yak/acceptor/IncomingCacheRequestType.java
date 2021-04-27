package org.guardiandev.yak.acceptor;

/**
 * The type of incoming request for a {@link org.guardiandev.yak.cacheprogression.CacheWrapper} to handle.
 */
public enum IncomingCacheRequestType {
  GET,
  CREATE,
  NOT_SUPPORTED
}
