package org.guardiandev.yak.cacheprogression;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

public final class CacheProgressionThread extends Thread {

  private final Collection<CacheWrapper> caches;
  private final AtomicBoolean isRunning;

  public CacheProgressionThread(final Collection<CacheWrapper> caches) {
    super("cache-progression-thread");
    this.caches = caches;
    this.isRunning = new AtomicBoolean(false);
  }

  @Override
  public synchronized void start() {
    super.start();
    isRunning.set(true);
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
