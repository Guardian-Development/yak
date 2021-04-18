package org.guardiandev.yak.cacheprogression;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

// TODO: basic connection wrapper logic
// TODO: finish up cache tick stuff

public final class CacheTickThread extends Thread {

  private final List<CacheWrapper> caches;
  private final AtomicBoolean isRunning;

  CacheTickThread(final List<CacheWrapper> caches) {
    super("cache-tick-thread");
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
        cache.tick();
      }
    }
  }
}
