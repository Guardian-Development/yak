package org.guardiandev.yak.config;

/**
 * Config POJO for a yak memory pool.
 */
public final class YakMemoryPoolConfig {

  private int poolSize;
  private boolean fillOnCreation;

  public int getPoolSize() {
    return poolSize;
  }

  public YakMemoryPoolConfig setPoolSize(int poolSize) {
    this.poolSize = poolSize;
    return this;
  }

  public boolean isFillOnCreation() {
    return fillOnCreation;
  }

  public YakMemoryPoolConfig setFillOnCreation(boolean fillOnCreation) {
    this.fillOnCreation = fillOnCreation;
    return this;
  }
}
