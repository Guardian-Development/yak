package org.guardiandev.yak;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class YakServerTest {

  @Test
  void shouldTestServer() {

    assertThat(new YakServer()).isNotNull();
  }
}
