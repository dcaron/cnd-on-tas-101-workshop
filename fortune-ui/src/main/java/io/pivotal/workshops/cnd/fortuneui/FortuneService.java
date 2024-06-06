package io.pivotal.workshops.cnd.fortuneui;

import java.time.Duration;

import io.github.resilience4j.core.functions.CheckedSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;

@Component
public class FortuneService {

  Logger log = LoggerFactory.getLogger(FortuneService.class);

  private final RestTemplate restTemplate;
  private final CircuitBreaker circuitBreaker;

  public FortuneService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
    this.circuitBreaker = createCircuitBreaker();
  }

  private CircuitBreaker createCircuitBreaker() {
    CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom().failureRateThreshold(50)
        .waitDurationInOpenState(Duration.ofMillis(20000)).build();
    CircuitBreaker circuitBreaker = CircuitBreaker.of("resilience-provider", circuitBreakerConfig);
    circuitBreaker.getEventPublisher().onSuccess(event -> log.info("Call success via circuit breaker"))
        .onCallNotPermitted(event -> log.info("Call denied by circuit breaker"))
        .onError(event -> log.info("Call failed via circuit breaker"));
    return circuitBreaker;
  }

  public String getFortune() {
    CheckedSupplier<String> someServiceCall = CircuitBreaker.decorateCheckedSupplier(circuitBreaker,
        () -> this.remoteFortune());
    try {
      return someServiceCall.get();
    } catch (Throwable e) {
      return this.defaultFortune();
    }
  }

  private String remoteFortune() {
    return restTemplate.getForObject("http://fortune-service", String.class);
  }

  private String defaultFortune() {
    return "Your future is unclear, please try later.";
  }

}
