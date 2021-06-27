package org.guardiandevelopment.yak.spring.controller;

import javax.validation.Valid;
import org.guardiandevelopment.generated.api.CacheApi;
import org.guardiandevelopment.yak.spring.service.CacheAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
class YakApiController implements CacheApi {

  private final CacheAccessor cacheAccessor;

  /*
    TODO: add unit testing for the cache object we setup
    TODO: check jacoco and code coverage all working nicely
   */

  YakApiController(final CacheAccessor cacheAccessor) {

    this.cacheAccessor = cacheAccessor;
  }

  @Override
  public Mono<ResponseEntity<Object>> getKeyName(final String cacheName, final String keyName, final ServerWebExchange exchange) {
    final var result = cacheAccessor.getValueInCache(cacheName, keyName);

    if (result.isEmpty()) {
      return Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    final var value = result.get();

    return Mono.just(new ResponseEntity<>(value.array(), HttpStatus.OK));
  }

  @Override
  public Mono<ResponseEntity<Void>> postKeyName(final String cacheName, final String keyName, @Valid final Mono<String> body, final ServerWebExchange exchange) {

    return body.map(b -> {
      final var result = cacheAccessor.saveValueInCache(cacheName, keyName, b);

      if (result) {
        return new ResponseEntity<>(HttpStatus.CREATED);
      }

      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    });
  }
}
