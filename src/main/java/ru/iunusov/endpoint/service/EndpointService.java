package ru.iunusov.endpoint.service;

import java.util.List;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.iunusov.endpoint.adapter.BackendAdapter;
import ru.iunusov.endpoint.service.dto.User;

@Component
public class EndpointService {

  private final BackendAdapter backendAdapter;

  @Value("${instance.id}")
  private int instanceId;

  @Value("${secret}")
  private String secret;

  private final Counter counter;

  private final Timer timer;

  public EndpointService(final BackendAdapter adapter, final MeterRegistry registry) {
    this.backendAdapter = adapter;
    counter = Counter.builder("user.errors.count")
        .tag("request", "users")
        .description("The number of request of user service")
        .register(registry);
    timer = Timer.builder("user.request.latency")
        .description("The latency of request of user service")
        .tag("request", "users")
        .register(registry);
  }

  @SneakyThrows
  public List<User> users() {
    try {
      return timer.recordCallable(backendAdapter::users);
    } catch (RuntimeException e) {
      counter.increment(1.0);
      throw e;
    }
  }
}