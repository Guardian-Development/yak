package org.guardiandevelopment.yak.server.acceptor;

import org.guardiandevelopment.yak.server.cacheprogression.CacheWrapper;

/**
 * The type of incoming request for a {@link CacheWrapper} to handle.
 */
public enum IncomingCacheRequestType {
  GET,
  CREATE,
  NOT_SUPPORTED
}
