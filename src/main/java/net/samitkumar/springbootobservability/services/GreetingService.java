package net.samitkumar.springbootobservability.services;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.samitkumar.springbootobservability.models.Greeting;
import org.springframework.stereotype.Service;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class GreetingService {
    private final Supplier<Long> latency = () -> new Random().nextLong(500);
    private final ObservationRegistry registry;
    private final MeterRegistry meterRegistry;
    public Mono<Greeting> greeting(String name) {

        var randomInt = new Random().nextInt(100);
        log.info("Random Ints={}", randomInt);
        //customised gauge
        AtomicInteger myGauge = meterRegistry.gauge("randomInt", new AtomicInteger(0));
        myGauge.set(randomInt);

        Long lat = latency.get();
        var result = switch (name) {
            case "FIRSTNAME" -> "John";
            case "MIDDLENAME" -> "Martin";
            case "LASTNAME" -> "Doe";
            default -> "UNKNOWN";
        };

        return Mono.just(new Greeting(result))
                .delayElement(Duration.ofMillis(lat))
                .name("GreetingService.greeting")
                .tag("type", name)
                .tag("latency", lat > 250 ? "high" : "low")
                .tap(Micrometer.observation(registry))
                ;
    }
}
