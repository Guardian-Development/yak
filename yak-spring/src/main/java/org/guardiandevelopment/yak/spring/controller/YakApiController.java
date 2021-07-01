package org.guardiandevelopment.yak.spring.controller;

import javax.validation.Valid;
import org.guardiandevelopment.generated.api.CacheApi;
import org.guardiandevelopment.yak.spring.service.CacheAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
class YakApiController implements CacheApi {

  private final CacheAccessor cacheAccessor;

  /*
    TODO: add unit testing for the cache object we setup
    TODO: check jacoco and code coverage all working nicely
    TODO: add feign client example
   */

  YakApiController(final CacheAccessor cacheAccessor) {

    this.cacheAccessor = cacheAccessor;
  }

  @Override
  public ResponseEntity<Object> getKeyName(final String cacheName, final String keyName) {
    final var result = cacheAccessor.getValueInCache(cacheName, keyName);

    if (result.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    final var value = result.get();

    return new ResponseEntity<>(value.array(), HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> postKeyName(final String cacheName, final String keyName, @Valid final String body) {

    final var result = cacheAccessor.saveValueInCache(cacheName, keyName, body);

    if (result) {
      return new ResponseEntity<>(HttpStatus.CREATED);
    }

    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }
}
