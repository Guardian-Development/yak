package yak.reference;

import java.util.List;

/**
 * Test class used for storing/retrieving from the cache.
 */
public final class TestCacheValue {

  public int id;
  public String field1;
  public List<Long> field2;
  public TestSubCacheValue field3;

  /**
   * Test class used for storing/retrieving from the cache.
   */
  public static final class TestSubCacheValue {

    public int subId;
    public String field2;
    public Double field3;
  }
}
