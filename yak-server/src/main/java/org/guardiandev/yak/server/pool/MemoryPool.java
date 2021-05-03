package org.guardiandev.yak.server.pool;

import java.util.function.Supplier;
import org.agrona.concurrent.ManyToManyConcurrentArrayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A memory pool of type T, where objects are taken out of the pool when in use, and returned to the pool for reuse.
 *
 * @param <T> the type of object to pool
 */
public final class MemoryPool<T> {

  private static final Logger LOG = LoggerFactory.getLogger(MemoryPool.class);

  private final Supplier<T> factory;
  private final ManyToManyConcurrentArrayQueue<T> pool;

  /**
   * Creates a memory pool of type T.
   *
   * @param factory        used to create each object in the pool
   * @param poolSize       the initial size of the pool to create
   * @param fillOnCreation whether to create the objects in the pool on creation or not
   */
  public MemoryPool(final Supplier<T> factory, final int poolSize, final boolean fillOnCreation) {

    this.factory = factory;
    this.pool = new ManyToManyConcurrentArrayQueue<>(poolSize);

    if (fillOnCreation) {
      for (var i = 0; i < poolSize; i++) {
        pool.add(factory.get());
      }
    }
  }

  /**
   * Take an object from the pool, if there is no remaining objects in the pool one is created.
   *
   * @return the object from the pool
   */
  public T take() {
    final var pooledObject = pool.poll();
    if (pooledObject == null) {
      LOG.debug("no available object in pool, creating new one");
      return factory.get();
    }

    return pooledObject;
  }

  /**
   * Return the object to the pool, making it available within the pool again.
   *
   * @param pooledObject the object to pool
   */
  public void returnToPool(final T pooledObject) {
    final var returned = pool.offer(pooledObject);
    if (!returned) {
      LOG.debug("memory pool is full, discarding object");
    }
  }
}
