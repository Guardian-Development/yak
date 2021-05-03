package org.guardiandev.yak.server.responder;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.agrona.LangUtil;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.OneToOneConcurrentArrayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for sending responses to requests.
 */
public final class ResponderThread extends Thread {

  private static final Logger LOG = LoggerFactory.getLogger(ResponderThread.class);

  private final AtomicBoolean isRunning;
  private final OneToOneConcurrentArrayQueue<Responder> outgoingResponses;
  private final IdleStrategy idleStrategy;
  private final ResponderToSelectorRegistration registerConnectionWithSelector;
  private Selector respondingSelector;

  /**
   * Creates the responder thread.
   *
   * @param idleStrategy the strategy to use to limit the thread when there is no work to execute
   */
  public ResponderThread(final IdleStrategy idleStrategy) {
    super("responder-thread");
    this.idleStrategy = idleStrategy;

    this.isRunning = new AtomicBoolean(false);
    this.outgoingResponses = new OneToOneConcurrentArrayQueue<>(100);
    this.registerConnectionWithSelector = new ResponderToSelectorRegistration();
  }

  /**
   * Marks the thread as running, opening the selector which handles the connections being managed.
   */
  @Override
  public synchronized void start() {
    LOG.info("starting responder thread");

    try {
      respondingSelector = Selector.open();

      isRunning.set(true);
      super.start();
    } catch (IOException e) {
      LOG.error("failed to open acceptor thread", e);
      LangUtil.rethrowUnchecked(e);
    }

    LOG.info("started responder thread");
  }

  /**
   * Marks the thread as not running, and closes the selector handling current connections.
   */
  @Override
  public void interrupt() {
    LOG.info("stopping responder thread due to interrupt");

    super.interrupt();
    isRunning.set(false);

    try {
      respondingSelector.close();
    } catch (IOException e) {
      LOG.error("failed to close responder thread on interrupt", e);
      LangUtil.rethrowUnchecked(e);
    }

    LOG.info("stopped responder thread due to interrupt");
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
   * Buffer a response to be handled by this thread.
   *
   * @param responder the responder to buffer
   * @return true if buffered successfully, else false.
   */
  public boolean bufferResponse(final Responder responder) {
    return outgoingResponses.offer(responder);
  }

  /**
   * Iterates the selector and progresses all ready connections.
   */
  @Override
  public void run() {
    while (isRunning.get()) {
      idleStrategy.idle(tick());
    }
  }

  private int tick() {

    outgoingResponses.drain(registerConnectionWithSelector);

    int numberAvailable = 0;
    try {
      numberAvailable = respondingSelector.selectNow();
    } catch (IOException e) {
      LOG.warn("unable to select connections to write from selector", e);
    }

    if (numberAvailable == 0) {
      return 0;
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

    return numberAvailable;
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

  private final class ResponderToSelectorRegistration implements Consumer<Responder> {

    @Override
    public void accept(Responder responder) {
      try {
        final var connection = responder.getSocket();
        connection.register(respondingSelector, SelectionKey.OP_WRITE, responder);
      } catch (ClosedChannelException e) {
        LOG.trace("failed to register responder with selector", e);
        closeConnection(responder);
      }
    }
  }
}
