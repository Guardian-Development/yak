package org.guardiandev.yak.acceptor;

import org.agrona.LangUtil;
import org.guardiandev.yak.cacheprogression.IncomingConnectionToCacheWrapperBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

// TODO: add a single 201 created integration test
// TODO: add a single 200 get integration test
// TODO: add a single 202 get integration test (no content)

public final class ConnectionAcceptorThread extends Thread {

  private static final Logger LOG = LoggerFactory.getLogger(ConnectionAcceptorThread.class);

  private final int port;
  private final IncomingConnectionFactory connectionFactory;
  private final IncomingConnectionToCacheWrapperBridge cacheWrapperBridge;
  private final AtomicBoolean isRunning;

  private ServerSocketChannel serverSocketChannel;
  private Selector acceptingSelector;

  public ConnectionAcceptorThread(final int port,
                                  final IncomingConnectionFactory connectionFactory,
                                  final IncomingConnectionToCacheWrapperBridge cacheWrapperBridge) {
    super("connection-acceptor-thread");

    this.port = port;
    this.connectionFactory = connectionFactory;
    this.cacheWrapperBridge = cacheWrapperBridge;
    this.isRunning = new AtomicBoolean(false);
  }

  @Override
  public synchronized void start() {
    LOG.debug("starting connection acceptor thread on port {}", port);

    try {
      final var address = new InetSocketAddress(port);
      serverSocketChannel = ServerSocketChannel.open();
      serverSocketChannel.configureBlocking(false);
      serverSocketChannel.bind(address);

      acceptingSelector = Selector.open();
      serverSocketChannel.register(acceptingSelector, SelectionKey.OP_ACCEPT);

      isRunning.set(true);
      super.start();
    } catch (IOException e) {
      LOG.error("failed to open acceptor thread", e);
      LangUtil.rethrowUnchecked(e);
    }

    LOG.debug("started connection acceptor thread on port {}", port);
  }

  @Override
  public void interrupt() {
    LOG.debug("stopping connection acceptor thread due to interrupt on port {}", port);

    super.interrupt();
    isRunning.set(false);

    try {
      acceptingSelector.close();
      serverSocketChannel.close();
    } catch (IOException e) {
      LOG.error("failed to close acceptor thread on interrupt", e);
      LangUtil.rethrowUnchecked(e);
    }

    LOG.debug("stopped connection acceptor thread due to interrupt on port {}", port);
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
    int numberAvailable = 0;
    try {
      numberAvailable = acceptingSelector.selectNow();
    } catch (IOException e) {
      LOG.warn("unable to select new connections from selector", e);
    }

    if (numberAvailable == 0) {
      return;
    }

    final var connectionsToProgress = acceptingSelector.selectedKeys();
    final var connectionIterator = connectionsToProgress.iterator();
    while (connectionIterator.hasNext()) {
      final var connection = connectionIterator.next();
      try {
        progressReadyConnection(connection);
      } catch (IOException e) {
        LOG.trace("failed to progress connection", e);
        // TODO: close the channel
        connection.cancel();
      }
      connectionIterator.remove();
    }
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
      final var request = incomingConnection.getRequest();
      connection.cancel();
      cacheWrapperBridge.acceptIncomingConnection(request);
    }
  }
}
