package org.guardiandev.yak.server.testsupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.agrona.LangUtil;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test class used when mocking read methods from byte buffers.
 */
public final class ByteLimitResponder implements Answer<Integer> {

  private final byte[] respondWith;
  private int position;
  private final int limit;

  /**
   * Create the responder with the content and limit.
   *
   * @param respondWith the full content to respond with
   * @param limit       the max number of bytes to respond with in a single iteration.
   */
  public ByteLimitResponder(final byte[] respondWith, final int limit) {
    this.respondWith = respondWith;
    this.limit = limit;
    this.position = 0;
  }

  @Override
  public Integer answer(InvocationOnMock invocation) {
    final var buffer = (ByteBuffer) invocation.getArgument(0);
    var bytesToWrite = Math.min(buffer.remaining(), limit);
    final var remainingBytes = respondWith.length - position;
    bytesToWrite = Math.min(bytesToWrite, remainingBytes);

    buffer.put(respondWith, position, bytesToWrite);
    position += bytesToWrite;
    return bytesToWrite;
  }

  /**
   * Return bytes from the mock channel, limiting the number of bytes returned in a single write call.
   *
   * @param channel the mocked channel to set up the response on
   * @param bytes   the bytes to return
   * @param limit   the maximum number of bytes to progress on a single write
   */
  public static void returnBytesFromMock(final SocketChannel channel, final byte[] bytes, final int limit) {
    try {
      when(channel.read(any(ByteBuffer.class))).thenAnswer(new ByteLimitResponder(bytes, limit));
    } catch (IOException e) {
      LangUtil.rethrowUnchecked(e);
    }
  }

  /**
   * Return string from the mock channel.
   *
   * @param channel  the mocked channel to set up the response on
   * @param response the string response to return from the write method
   */
  public static void returnBytesFromMock(final SocketChannel channel, final String response) {
    returnBytesFromMock(channel, response.getBytes(), response.getBytes().length);
  }

  /**
   * Return string from the mock channel, limiting the number of bytes returned in a single write call.
   *
   * @param channel  the mocked channel to set up the response on
   * @param response the string response to return from the write method
   * @param limit    the maximum number of bytes to progress on a single write
   */
  public static void returnBytesFromMock(final SocketChannel channel, final String response, final int limit) {
    returnBytesFromMock(channel, response.getBytes(), limit);
  }
}
