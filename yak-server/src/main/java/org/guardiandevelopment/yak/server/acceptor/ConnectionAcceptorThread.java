package org.guardiandevelopment.yak.server.acceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import org.agrona.LangUtil;
import org.agrona.concurrent.IdleStrategy;
import org.guardiandevelopment.yak.server.metrics.ThreadMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for accepting and reading the initial request in order to know how to process it downstream.
 */
public final class ConnectionAcceptorThread extends Thread {

  private static final Logger LOG = LoggerFactory.getLogger(ConnectionAcceptorThread.class);

  private final int port;
  private final IncomingConnectionFactory connectionFactory;
  private final HttpRequestProcessor httpRequestProcessor;
  private final IdleStrategy idleStrategy;
  private final ThreadMetrics threadMetrics;
  private final AtomicBoolean isRunning;

  private ServerSocketChannel serverSocketChannel;
  private Selector acceptingSelector;
  private InetSocketAddress listeningOnAddress;

  /**
   * Initialise the connection acceptor thread.
   *
   * @param port                 the port to listen for connections on
   * @param connectionFactory    the factory to use when wrapping a new accepted connection
   * @param httpRequestProcessor used for routing the http connection to the correct internal thread
   * @param idleStrategy         the strategy to use to limit the thread when there is no work to execute
   * @param threadMetrics        metrics for observing thread health
   */
  public ConnectionAcceptorThread(final int port,
                                  final IncomingConnectionFactory connectionFactory,
                                  final HttpRequestProcessor httpRequestProcessor,
                                  final IdleStrategy idleStrategy,
                                  final ThreadMetrics threadMetrics) {
    super("connection-acceptor-thread");

    this.port = port;
    this.connectionFactory = connectionFactory;
    this.httpRequestProcessor = httpRequestProcessor;
    this.idleStrategy = idleStrategy;
    this.threadMetrics = threadMetrics;
    this.isRunning = new AtomicBoolean(false);
  }

  /**
   * Marks the thread as running, opening the socket to accept connections on.
   */
  @Override
  public synchronized void start() {
    LOG.info("starting connection acceptor thread on port {}", port);

    try {
      final var address = new InetSocketAddress(port);
      serverSocketChannel = ServerSocketChannel.open();
      serverSocketChannel.configureBlocking(false);
      serverSocketChannel.bind(address);

      acceptingSelector = Selector.open();
      serverSocketChannel.register(acceptingSelector, SelectionKey.OP_ACCEPT);
      listeningOnAddress = address;

      isRunning.set(true);
      super.start();
    } catch (IOException e) {
      LOG.error("failed to open acceptor thread", e);
      LangUtil.rethrowUnchecked(e);
    }

    LOG.info("started connection acceptor thread on port {}", port);
  }

  /**
   * Marks the thread as not running, and closes the server socket to stop accepting connections.
   */
  @Override
  public void interrupt() {
    LOG.info("stopping connection acceptor thread on port {} due to interrupt", port);

    super.interrupt();
    isRunning.set(false);

    try {
      acceptingSelector.close();
      serverSocketChannel.close();
    } catch (IOException e) {
      LOG.error("failed to close acceptor thread on interrupt", e);
      LangUtil.rethrowUnchecked(e);
    }

    LOG.info("stopped connection acceptor thread on port {} due to interrupt", port);
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
   * Gets the socket the server is accepting connections on.
   *
   * @return the address of the socket, or null if the thread has not been starteds
   */
  public InetSocketAddress getListeningOnAddress() {
    return listeningOnAddress;
  }

  /**
   * Run the thread, which iterates the selector and progresses new connections.
   */
  @Override
  public void run() {
    while (isRunning.get()) {
      threadMetrics.incHeartbeat();
      idleStrategy.idle(tick());
    }
  }

  /**
   * Perform a single iteration of the thread, accepting and progressing any ready connections.
   */
  private int tick() {
    int numberAvailable = 0;
    try {
      numberAvailable = acceptingSelector.selectNow();
    } catch (IOException | ClosedSelectorException e) {
      LOG.warn("unable to select new connections from selector", e);
    }

    if (numberAvailable == 0) {
      return 0;
    }

    final var connectionsToProgress = acceptingSelector.selectedKeys();
    final var connectionIterator = connectionsToProgress.iterator();

    while (connectionIterator.hasNext()) {
      final var connection = connectionIterator.next();
      try {
        progressReadyConnection(connection);
      } catch (Exception e) {
        LOG.trace("failed to progress connection", e);
        closeConnection(connection);
      }

      connectionIterator.remove();
    }

    return numberAvailable;
  }

  private void progressReadyConnection(final SelectionKey connection) throws IOException {
    if (connection.isAcceptable()) {
      progressAcceptableConnection(connection);
    }
    if (connection.isReadable()) {
      progressReadableConnection(connection);
    }
  }

  private void progressAcceptableConnection(final SelectionKey connection) throws IOException {
    final var channel = (ServerSocketChannel) connection.channel();
    final var newConnection = channel.accept();

    if (newConnection != null) {
      LOG.trace("[{}] accepted new connection", newConnection.getRemoteAddress());
      newConnection.configureBlocking(false);
      final var incomingConnection = connectionFactory.wrapConnection(newConnection);
      newConnection.register(acceptingSelector, SelectionKey.OP_READ, incomingConnection);
    }
  }

  private void progressReadableConnection(final SelectionKey connection) throws IOException {
    final var incomingConnection = (IncomingConnection) connection.attachment();
    final var complete = incomingConnection.progress();
    if (complete) {
      if (incomingConnection.hasError()) {
        closeConnection(connection);
        return;
      }

      final var request = incomingConnection.getRequest();
      final var rawConnection = (SocketChannel) connection.channel();
      httpRequestProcessor.processReadyRequest(request, rawConnection);

      incomingConnection.cleanup();
      connection.cancel();
    }
  }

  private void closeConnection(final SelectionKey connection) {
    try {
      final var incomingConnection = (IncomingConnection) connection.attachment();
      if (incomingConnection != null) {
        incomingConnection.cleanup();
      }
      connection.cancel();
      connection.channel().close();
    } catch (IOException ex) {
      LOG.trace("failed to close connection, abandoning", ex);
    }
  }
}
