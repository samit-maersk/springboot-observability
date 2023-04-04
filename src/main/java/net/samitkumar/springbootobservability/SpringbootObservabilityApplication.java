package net.samitkumar.springbootobservability;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
public class SpringbootObservabilityApplication {
	final WebClient.Builder builder;
	public static void main(String[] args) {
		SpringApplication.run(SpringbootObservabilityApplication.class, args);
	}

	/*
	@Bean
	ObservationTextPublisher otp() {
		return new ObservationTextPublisher();
	}*/

	/*public Mono<String> getClaims() {
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
	}*/

	@Bean
	public WebClient jsonPlaceHolderWebClient() {
		return builder
				.baseUrl("https://jsonplaceholder.typicode.com/")
				.defaultHeader("content-type", "application/json")
				.build();
	}
}

