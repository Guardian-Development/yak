package org.guardiandevelopment.yak.spring.config;

import java.util.List;
import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Root level config for custom properties required by the server.
 */
@Component
@ConfigurationProperties("yak")
public class YakServerConfig {

  @NotNull
  private List<YakCacheConfig> caches;

  public List<YakCacheConfig> getCaches() {
    return caches;
  }

  public YakServerConfig setCaches(List<YakCacheConfig> caches) {
    this.caches = caches;
    return this;
  }
}
