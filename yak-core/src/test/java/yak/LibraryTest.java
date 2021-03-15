package yak;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LibraryTest {

  @Test
  public void testSomeLibraryMethod() {
    Library classUnderTest = new Library();
    assertTrue(classUnderTest.someLibraryMethod(), "someLibraryMethod should return 'true'");
  }
}
