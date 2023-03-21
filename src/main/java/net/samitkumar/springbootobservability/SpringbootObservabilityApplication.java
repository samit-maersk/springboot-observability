package net.samitkumar.springbootobservability;

import io.micrometer.observation.ObservationRegistry;
//import io.micrometer.observation.ObservationTextPublisher;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
//import org.springframework.security.core.context.ReactiveSecurityContextHolder;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
//import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

@SpringBootApplication
public class SpringbootObservabilityApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootObservabilityApplication.class, args);
	}

	/*
	@Bean
	ObservationTextPublisher otp() {
		return new ObservationTextPublisher();
	}*/

	@Bean
	RouterFunction route(GreetingService service) {
		return RouterFunctions
				.route()
				.GET("/hello/{name}", request -> {
					var name = request.pathVariable("name");
					return ServerResponse.ok().body(service.greeting(name), Greeting.class);
				})
				.build();
	}
}

record Greeting(String name) {
	@Builder
	public Greeting {}
}

@Service
@RequiredArgsConstructor
class GreetingService {
	private final Supplier<Long> latency = () -> new Random().nextLong(500);
	private final ObservationRegistry registry;

	public Mono<Greeting> greeting(String name) {
		Long lat = latency.get();
		return Mono.just(new Greeting(name))
				.delayElement(Duration.ofMillis(lat))
				;
	}
	/*
	public Mono<String> getClaims() {
		return ReactiveSecurityContextHolder
				.getContext()
				.map(SecurityContext::getAuthentication)
				.map(authentication -> Optional.ofNullable(authentication).orElseThrow())
				.map(JwtAuthenticationToken.class::cast)
				.map(JwtAuthenticationToken::getToken)
				.map(Jwt::getClaims)
				.map(claims -> claims.get("carrier"))
				.map(String.class::cast)
				.flatMap(carrier -> Mono.just(carrier));
	}
	*/
}