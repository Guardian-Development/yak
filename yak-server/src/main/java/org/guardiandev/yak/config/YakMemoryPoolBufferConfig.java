package org.guardiandev.yak.config;

/**
 * Config POJO for a yak memory buffer pool.
 */
public final class YakMemoryPoolBufferConfig {

  private int poolSize;
  private boolean fillOnCreation;
  private int bufferSize;

  public int getPoolSize() {
    return poolSize;
  }

  public YakMemoryPoolBufferConfig setPoolSize(int poolSize) {
    this.poolSize = poolSize;
    return this;
  }

  public boolean isFillOnCreation() {
    return fillOnCreation;
  }

  public YakMemoryPoolBufferConfig setFillOnCreation(boolean fillOnCreation) {
    this.fillOnCreation = fillOnCreation;
    return this;
  }

  public int getBufferSize() {
    return bufferSize;
  }

  public YakMemoryPoolBufferConfig setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
    return this;
  }
}
