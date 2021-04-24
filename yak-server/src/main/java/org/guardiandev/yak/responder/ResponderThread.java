package org.guardiandev.yak.responder;

import org.agrona.LangUtil;
import org.agrona.concurrent.OneToOneConcurrentArrayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ResponderThread extends Thread {

  private static final Logger LOG = LoggerFactory.getLogger(ResponderThread.class);

  private final AtomicBoolean isRunning;
  private final OneToOneConcurrentArrayQueue<Responder> outgoingResponses;
  private Selector respondingSelector;

  public ResponderThread() {
    super("responder-thread");

    this.isRunning = new AtomicBoolean(false);
    this.outgoingResponses = new OneToOneConcurrentArrayQueue<>(100);
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

  public boolean bufferResponse(final Responder responder) {
    return outgoingResponses.offer(responder);
  }

  @Override
  public void run() {
    while (isRunning.get()) {
      tick();
    }
  }

  private void tick() {
    outgoingResponses.drain(this::registerConnectionWithSelector);

    int numberAvailable = 0;
    try {
      numberAvailable = respondingSelector.selectNow();
    } catch (IOException e) {
      LOG.warn("unable to select connections to write from selector", e);
    }

    if (numberAvailable == 0) {
      return;
    }

    final var connectionsToProgress = respondingSelector.selectedKeys();
    final var connectionIterator = connectionsToProgress.iterator();

    while (connectionIterator.hasNext()) {
      final var connection = connectionIterator.next();
      try {
        progressReadyConnection(connection);
      } catch (IOException e) {
        LOG.trace("failed to progress connection", e);
        closeConnection(connection);
      }

      connectionIterator.remove();
    }
  }

  private void progressReadyConnection(final SelectionKey connection) throws IOException {
    if (connection.isWritable()) {
      final var responder = (Responder) connection.attachment();
      final var complete = responder.progress();
      if (complete) {
        closeConnection(connection);
      }
    }
  }

  private void registerConnectionWithSelector(final Responder responder) {
    try {
      final var connection = responder.getSocket();
      connection.register(respondingSelector, SelectionKey.OP_WRITE, responder);
    } catch (ClosedChannelException e) {
      LOG.trace("failed to register responder with selector", e);
      closeConnection(responder);
    }
  }

  private void closeConnection(final Responder responder) {
    try {
      responder.getSocket().close();
      responder.cleanup();
    } catch (IOException ex) {
      LOG.trace("failed to close connection, abandoning", ex);
    }
  }

  private void closeConnection(final SelectionKey connection) {
    try {
      final var responder = (Responder) connection.attachment();
      if (responder != null) {
        responder.cleanup();
      }
      connection.cancel();
      connection.channel().close();
    } catch (IOException ex) {
      LOG.trace("failed to close connection, abandoning", ex);
    }
  }
}
