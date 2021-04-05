package org.guardiandev.yak.config;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads the yak config from a JSON file, being as lenient as possible in data validation.
 */
public final class YakConfigFromJsonBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(YakConfigFromJsonBuilder.class);

  private final Path filePath;
  private final ObjectMapper mapper;

  YakConfigFromJsonBuilder(final Path filePath) {
    this.filePath = filePath;
    this.mapper = new ObjectMapper();
  }

  /**
   * Loads the config from a JSON file at {@link #filePath}.
   *
   * @return the loaded yak config from JSON file
   * @throws IOException if we are unable to read the json file
   */
  public YakServerConfig load() throws IOException {
    LOG.trace("attempting to read config from file {}", filePath.toAbsolutePath());

    final var file = filePath.toFile();
    if (!file.exists() || file.isDirectory()) {
      LOG.error("unable to load config, the path of {} does not exist or is a directory", filePath);
    }

    try {
      final var result = mapper.readValue(file, YakServerConfig.class);
      LOG.trace("successfully read config from file {}", filePath.toAbsolutePath());
      return result;
    } catch (final JacksonException e) {
      LOG.error("failed to load config from file {}", filePath.toAbsolutePath(), e);
      throw e;
    }
  }
}
