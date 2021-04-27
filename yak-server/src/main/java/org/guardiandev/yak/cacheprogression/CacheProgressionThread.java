package org.guardiandev.yak.cacheprogression;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Progresses the collection of caches assigned to the thread each loop, allowing for requests to be executed.
 */
public final class CacheProgressionThread extends Thread {

  private static final Logger LOG = LoggerFactory.getLogger(CacheProgressionThread.class);

  private final Collection<CacheWrapper> caches;
  private final AtomicBoolean isRunning;

  /**
   * Initialise the cache progression thread.
   *
   * @param caches the caches assigned to be progressed by this thread
   */
  public CacheProgressionThread(final Collection<CacheWrapper> caches) {
    super("cache-progression-thread");
    this.caches = caches;
    this.isRunning = new AtomicBoolean(false);
  }

  /**
   * Marks the thread as running.
   */
  @Override
  public synchronized void start() {
    LOG.debug("starting cache progression thread");

    isRunning.set(true);
    super.start();

    LOG.debug("started cache progression thread");
  }

  /**
   * Marks the thread as not running.
   */
  @Override
  public void interrupt() {
    super.interrupt();
    isRunning.set(false);
  }

  /**
   * Whether the thread is interrupted, or is not running.
   *
   * @return true if running, else false
   */
  @Override
  public boolean isInterrupted() {
    return super.isInterrupted() || !isRunning.get();
  }

  /**
   * Run the thread, which iterates the caches and executes any buffered requests.
   */
  @Override
  public void run() {
    while (isRunning.get()) {
      for (CacheWrapper cache : caches) {
        cache.progressIncomingRequests();
      }
    }
  }
}
