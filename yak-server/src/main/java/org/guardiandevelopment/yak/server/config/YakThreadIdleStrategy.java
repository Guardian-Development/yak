package org.guardiandevelopment.yak.server.config;

/**
 * Config POJO for how to idle any thread when there is no work.
 */
public final class YakThreadIdleStrategy {

  private long maxSpins;
  private long maxYields;
  private long minParkPeriodNs;
  private long maxParkPeriodNs;

  public long getMaxSpins() {
    return maxSpins;
  }

  public YakThreadIdleStrategy setMaxSpins(long maxSpins) {
    this.maxSpins = maxSpins;
    return this;
  }

  public long getMaxYields() {
    return maxYields;
  }

  public YakThreadIdleStrategy setMaxYields(long maxYields) {
    this.maxYields = maxYields;
    return this;
  }

  public long getMinParkPeriodNs() {
    return minParkPeriodNs;
  }

  public YakThreadIdleStrategy setMinParkPeriodNs(long minParkPeriodNs) {
    this.minParkPeriodNs = minParkPeriodNs;
    return this;
  }

  public long getMaxParkPeriodNs() {
    return maxParkPeriodNs;
  }

  public YakThreadIdleStrategy setMaxParkPeriodNs(long maxParkPeriodNs) {
    this.maxParkPeriodNs = maxParkPeriodNs;
    return this;
  }
}
