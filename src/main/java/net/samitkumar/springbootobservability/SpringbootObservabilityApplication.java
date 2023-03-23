package net.samitkumar.springbootobservability;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.micrometer.observation.ObservationRegistry;
//import io.micrometer.observation.ObservationTextPublisher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
//import org.springframework.security.core.context.ReactiveSecurityContextHolder;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
//import java.util.Optional;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import static org.springframework.util.StringUtils.hasText;

@SpringBootApplication
@Slf4j
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
	RouterFunction route(Handlers handlers) {
		return RouterFunctions
				.route()
				.GET("hello/{name}", handlers::hello)
				.path("/user", pathBuilder -> pathBuilder
						.GET("", handlers::user)
						.GET("/{id}", handlers::userById))
				.build();
	}

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
}

//Handlers
@Configuration
@RequiredArgsConstructor
class Handlers {
	final GreetingService greetingService;
	final UserService userService;
	public Mono<ServerResponse> hello(ServerRequest request) {
		var name = request.pathVariable("name");
		if (hasText(name)) {
			return ServerResponse.ok().body(greetingService.greeting(name), List.class);
		}
		var name1 = request.queryParam("name").orElse("default name");
		return ServerResponse.ok().body(greetingService.greeting(name1), Greeting.class);
	}

	public Mono<ServerResponse> user(ServerRequest request) {
		return ServerResponse.ok().body(userService.allUser(), Flux.class);
	}

	public Mono<ServerResponse> userById(ServerRequest request) {
		var userId = request.pathVariable("id");
		return ServerResponse.ok().body(userService.userById(Integer.valueOf(userId)), Users.class);
	}
}

//model
record Greeting(String name) {
	@Builder
	public Greeting {}
}
record Posts(int userId,int id,String title, String body) {
	@Builder public Posts {}
}
record Comments(int postId, int id, String name,String email,String body) {
	@Builder public Comments {}
}

record Albums(int userId, int id, String title, List<Photos> photos) {
	@Builder public Albums {}
}

record Photos(int albumId, int id, String title, String url, String thumbnailUrl, List<Comments> comments) {
	@Builder public Photos {}
}

record Todos(int userId, int id, String title, String completed) {
	@Builder public Todos {}
}

record Users(int id, String name, String username, String email, Address address, String phone, String website, Company company, List<Posts> posts, List<Albums> albums, List<Todos> todos) {
	@Builder Users {}
	record Address(String street, String suite, String city, String zipcode, Geo geo) {
		@Builder Address {}
		record Geo(String lat, String lng) {
			@Builder public Geo {}
		}
	}
	record Company(String name, String catchPhrase, String bs) {
		@Builder public Company {}
	}
}

@Configuration
class WebClientConfig {
	@Bean
	WebClient jsonPlaceHolderWebClient() {
		return WebClient.builder()
				.baseUrl("https://jsonplaceholder.typicode.com/")
				.defaultHeader("content-type", "application/json")
				.build();
	}
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
}

@Service
@RequiredArgsConstructor
@Slf4j
class UserService {
	final WebClient jsonPlaceHolderWebClient;
	final PostsService postsService;
	final AlbumService albumService;
	final TodoService todoService;
	public Flux<Users> allUser() {
		return jsonPlaceHolderWebClient
				.get()
				.uri("/users")
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToFlux(Users.class)
				.onErrorResume(e -> {
					log.error("UserService.allUser {}",e.getMessage());
					return Flux.empty();
				});
	}

	public Mono<Users> userById(int id) {
		return jsonPlaceHolderWebClient
				.get()
				.uri("/users", uriBuilder -> uriBuilder.queryParam("id", id).build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToFlux(Users.class)
				.onErrorResume(e -> {
					log.error("UserService.userById {}", e.getMessage());
					return Mono.empty();
				})
				.next()
				/*
				.flatMap(user -> {
					//do multiple call ang gather all other necessary data

				})*/;
	}


}

@Service
@RequiredArgsConstructor
@Slf4j
class PostsService {
	final WebClient jsonPlaceHolderWebClient;
	public Flux<Posts> byUserId(String id) {
		return jsonPlaceHolderWebClient
				.get()
				.uri("/posts", uriBuilder -> uriBuilder.queryParam("userId", id).build())
				.retrieve()
				.bodyToFlux(Posts.class)
				.onErrorResume(e -> {
					log.error("PostsService.byUserId {}", e.getMessage());
					return Flux.empty();
				});
	}
}

@Service
@RequiredArgsConstructor
@Slf4j
class AlbumService {
	final WebClient jsonPlaceHolderWebClient;
	public Flux<Albums> byUserId(String id) {
		return jsonPlaceHolderWebClient
				.get()
				.uri("/albums", uriBuilder -> uriBuilder.queryParam("userId", id).build())
				.retrieve()
				.bodyToFlux(Albums.class)
				.onErrorResume(e -> {
					log.error("AlbumService.byUserId {}", e.getMessage());
					return Flux.empty();
				});
	}
}
@Service
@RequiredArgsConstructor
@Slf4j
class TodoService {
	final WebClient jsonPlaceHolderWebClient;
	public Flux<Todos> byUserId(String id) {
		return jsonPlaceHolderWebClient
				.get()
				.uri("/todos", uriBuilder -> uriBuilder.queryParam("userId", id).build())
				.retrieve()
				.bodyToFlux(Todos.class)
				.onErrorResume(e -> {
					log.error("AlbumService.byUserId {}", e.getMessage());
					return Flux.empty();
				});
	}
}

@Service
@RequiredArgsConstructor
@Slf4j
class CommentsService {
	final WebClient jsonPlaceHolderWebClient;
}

@Service
@RequiredArgsConstructor
@Slf4j
class PhotosService {
	final WebClient jsonPlaceHolderWebClient;
}