package org.guardiandevelopment.yak.server.testsupport;

import java.nio.ByteBuffer;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

/**
 * Provides easy assertions of byte buffers.
 */
public final class ByteBufferAssertion extends AbstractAssert<ByteBufferAssertion, ByteBuffer> {

  private ByteBufferAssertion(final ByteBuffer actual) {
    super(actual, ByteBufferAssertion.class);
  }

  /**
   * Factory method for assertions of byte buffers.
   *
   * @param actual the actual buffer received
   * @return byte buffer assertion
   */
  public static ByteBufferAssertion assertThat(final ByteBuffer actual) {
    return new ByteBufferAssertion(actual);
  }

  /**
   * Assert the buffer contains only the expected string.
   *
   * @param expected the expected contents of the buffer
   * @return self
   */
  public ByteBufferAssertion isEqualTo(final String expected) {
    final var copy = actual.asReadOnlyBuffer();

    final var builder = new StringBuilder(expected.length());

    while (copy.hasRemaining()) {
      builder.append((char) copy.get());
    }

    final var actualString = builder.toString();

    Assertions.assertThat(actualString).isEqualTo(expected);

    return this;
  }
}
