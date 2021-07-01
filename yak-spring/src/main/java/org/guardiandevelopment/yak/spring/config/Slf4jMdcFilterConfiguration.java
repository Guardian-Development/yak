package org.guardiandevelopment.yak.spring.config;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Responsible for setting the requestId and clientIp in the MDC context for logging.
 * <p>
 * Note: this relies on using a thread-per-request model, otherwise the MDC content wont be
 * propagated correctly.
 * </p>
 * <p>
 * Use as follows in log4j2 pattern:
 * %X{RequestIdFilter.UUID}
 * %X{RequestIdFilter.ClientIp}
 * </p>
 */
@Configuration
class Slf4jMdcFilterConfiguration {

  private static final String MDC_UUID_TOKEN_KEY = "RequestIdFilter.UUID";
  private static final String MDC_CLIENT_IP_KEY = "RequestIdFilter.ClientIp";
  private static final String REQUEST_ID_HEADER_KEY = "X-Request-Id";
  private static final String FORWARDED_FOR_HEADER_KEY = "X-Forwarded-For";

  /**
   * Registers the log4j requestId filter within the sprint context.
   *
   * @return a spring filter for setting the requestId in the MDC context
   */
  @Bean
  public FilterRegistrationBean<Slf4jRequestIdMdcFilter> requestIdLoggingBean() {
    final var registrationBean = new FilterRegistrationBean<Slf4jRequestIdMdcFilter>();
    final var requestIdMdcFilter = new Slf4jRequestIdMdcFilter();
    registrationBean.setFilter(requestIdMdcFilter);
    registrationBean.setOrder(2);
    return registrationBean;
  }

  private static final class Slf4jRequestIdMdcFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
      try {
        final var requestId = extractRequestId(request);
        final var clientIp = extractClientIp(request);
        MDC.put(MDC_CLIENT_IP_KEY, clientIp);
        MDC.put(MDC_UUID_TOKEN_KEY, requestId);
        response.addHeader(REQUEST_ID_HEADER_KEY, requestId);
        filterChain.doFilter(request, response);
      } finally {
        MDC.remove(MDC_CLIENT_IP_KEY);
        MDC.remove(MDC_UUID_TOKEN_KEY);
      }
    }

    private String extractRequestId(final HttpServletRequest request) {
      final var requestId = request.getHeader(REQUEST_ID_HEADER_KEY);
      if (StringUtils.hasText(requestId)) {
        return requestId;
      }

      return UUID.randomUUID().toString();
    }

    private String extractClientIp(final HttpServletRequest request) {
      final var clientIp = request.getHeader(FORWARDED_FOR_HEADER_KEY);

      if (StringUtils.hasText(clientIp)) {
        return clientIp.split(",")[0];
      }

      return request.getRemoteAddr();
    }
  }
}
