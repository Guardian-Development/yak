package org.guardiandev.yak.testsupport;

import org.agrona.LangUtil;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public final class ByteLimitResponder implements Answer<Integer> {

  private final byte[] respondWith;
  private int position;
  private final int limit;

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

  public static void returnBytesFromMock(final SocketChannel channel, final byte[] bytes, final int limit) {
    try {
      when(channel.read(any(ByteBuffer.class))).thenAnswer(new ByteLimitResponder(bytes, limit));
    } catch (IOException e) {
      LangUtil.rethrowUnchecked(e);
    }
  }

  public static void returnBytesFromMock(final SocketChannel channel, final String response) {
    returnBytesFromMock(channel, response.getBytes(), response.getBytes().length);
  }

  public static void returnBytesFromMock(final SocketChannel channel, final String response, final int limit) {
    returnBytesFromMock(channel, response.getBytes(), limit);
  }
}
