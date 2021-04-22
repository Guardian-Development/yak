package org.guardiandev.yak.cacheprogression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

public final class CacheProgressionThread extends Thread {

  private static final Logger LOG = LoggerFactory.getLogger(CacheProgressionThread.class);

  private final Collection<CacheWrapper> caches;
  private final AtomicBoolean isRunning;

  public CacheProgressionThread(final Collection<CacheWrapper> caches) {
    super("cache-progression-thread");
    this.caches = caches;
    this.isRunning = new AtomicBoolean(false);
  }

  @Override
  public synchronized void start() {
    LOG.debug("starting cache progression thread");

    isRunning.set(true);
    super.start();

    LOG.debug("started cache progression thread");
  }

  @Override
  public void interrupt() {
    super.interrupt();
    isRunning.set(false);
  }

  @Override
  public boolean isInterrupted() {
    return super.isInterrupted() || !isRunning.get();
  }

  @Override
  public void run() {
    while (isRunning.get()) {
      for (CacheWrapper cache : caches) {
        cache.progressIncomingRequests();
      }
    }
  }
}
