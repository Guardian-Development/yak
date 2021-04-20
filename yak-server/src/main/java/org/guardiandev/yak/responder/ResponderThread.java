package org.guardiandev.yak.responder;

import org.agrona.LangUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ResponderThread extends Thread {

  private static final Logger LOG = LoggerFactory.getLogger(ResponderThread.class);

  private final AtomicBoolean isRunning;
  private Selector respondingSelector;

  ResponderThread() {
    super("responder-thread");

    this.isRunning = new AtomicBoolean(false);
  }

  @Override
  public synchronized void start() {
    LOG.debug("starting responder thread");

    try {
      respondingSelector = Selector.open();

      isRunning.set(true);
      super.start();
    } catch (IOException e) {
      LOG.error("failed to open acceptor thread", e);
      LangUtil.rethrowUnchecked(e);
    }

    LOG.debug("started responder thread");
  }

  @Override
  public void interrupt() {
    LOG.debug("stopping responder thread due to interrupt");

    super.interrupt();
    isRunning.set(false);

    try {
      respondingSelector.close();
    } catch (IOException e) {
      LOG.error("failed to close responder thread on interrupt", e);
      LangUtil.rethrowUnchecked(e);
    }

    LOG.debug("stopped responder thread due to interrupt");
  }

  @Override
  public boolean isInterrupted() {
    return super.isInterrupted() || !isRunning.get();
  }

  @Override
  public void run() {
    while (isRunning.get()) {
      tick();
    }
  }

  private void tick() {

  }
}
