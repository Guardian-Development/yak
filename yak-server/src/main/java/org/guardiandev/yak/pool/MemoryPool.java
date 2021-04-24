package org.guardiandev.yak.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.function.Supplier;

public final class MemoryPool<T> {

  private static final Logger LOG = LoggerFactory.getLogger(MemoryPool.class);

  private final Supplier<T> factory;
  private final ArrayDeque<T> pool;

  public MemoryPool(final Supplier<T> factory, final int poolSize, final boolean fillOnCreation) {

    this.factory = factory;
    this.pool = new ArrayDeque<>(poolSize);

    if (fillOnCreation) {
      for (var i = 0; i < poolSize; i++) {
        pool.add(factory.get());
      }
    }
  }

  public T take() {
    final var pooledObject = pool.poll();
    if (pooledObject == null) {
      LOG.warn("no available object in pool, creating new one");
      return factory.get();
    }

    return pooledObject;
  }

  public void returnToPool(final T pooledObject) {
    pool.addLast(pooledObject);
  }
}
