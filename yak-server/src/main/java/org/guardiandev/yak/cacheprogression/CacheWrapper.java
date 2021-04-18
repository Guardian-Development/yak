package org.guardiandev.yak.cacheprogression;

import org.guardiandev.yak.YakCache;

import java.nio.ByteBuffer;

public final class CacheWrapper {

  private final YakCache<String, ByteBuffer> cache;

  CacheWrapper(final YakCache<String, ByteBuffer> cache) {

    this.cache = cache;
  }

  public boolean bufferRequest() {

    // TODO: need to know the param in order to know how to respond, i.e. we need the underlying connection
    //  then can add in the one to one ring buffer
    return true;
  }

  public void tick() {

  }
}
