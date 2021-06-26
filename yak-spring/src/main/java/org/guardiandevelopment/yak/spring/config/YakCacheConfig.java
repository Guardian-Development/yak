package org.guardiandevelopment.yak.spring.config;

import javax.validation.constraints.NotNull;

/**
 * Config POJO for a yak cache.
 */
public final class YakCacheConfig {

  @NotNull
  private String name;

  @NotNull
  private Integer maximumKeys;

  @NotNull
  private Integer fixedValueSize;

  @NotNull
  private String valueStorageMechanism;

  @NotNull
  private String evictionStrategy;

  public String getName() {
    return name;
  }

  public YakCacheConfig setName(String name) {
    this.name = name;
    return this;
  }

  public Integer getMaximumKeys() {
    return maximumKeys;
  }

  public YakCacheConfig setMaximumKeys(Integer maximumKeys) {
    this.maximumKeys = maximumKeys;
    return this;
  }

  public Integer getFixedValueSize() {
    return fixedValueSize;
  }

  public YakCacheConfig setFixedValueSize(Integer fixedValueSize) {
    this.fixedValueSize = fixedValueSize;
    return this;
  }

  public String getValueStorageMechanism() {
    return valueStorageMechanism;
  }

  public YakCacheConfig setValueStorageMechanism(String valueStorageMechanism) {
    this.valueStorageMechanism = valueStorageMechanism;
    return this;
  }

  public String getEvictionStrategy() {
    return evictionStrategy;
  }

  public YakCacheConfig setEvictionStrategy(String evictionStrategy) {
    this.evictionStrategy = evictionStrategy;
    return this;
  }
}