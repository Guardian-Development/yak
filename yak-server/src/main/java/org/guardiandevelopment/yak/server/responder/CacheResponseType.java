package org.guardiandevelopment.yak.server.responder;

/**
 * The type of response that needs to be sent to the client.
 */
public enum CacheResponseType {
  FOUND,
  KEY_NOT_FOUND,
  CACHE_NOT_FOUND,
  CREATED
}
