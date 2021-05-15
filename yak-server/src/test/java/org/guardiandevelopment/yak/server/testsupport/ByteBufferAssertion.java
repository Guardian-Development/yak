package org.guardiandevelopment.yak.server.testsupport;

import java.nio.ByteBuffer;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

public final class ByteBufferAssertion extends AbstractAssert<ByteBufferAssertion, ByteBuffer> {

  public ByteBufferAssertion(final ByteBuffer actual) {
    super(actual, ByteBufferAssertion.class);
  }

  public static ByteBufferAssertion assertThat(final ByteBuffer actual) {
    return new ByteBufferAssertion(actual);
  }

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
