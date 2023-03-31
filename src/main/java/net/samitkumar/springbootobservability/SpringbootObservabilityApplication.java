package net.samitkumar.springbootobservability;

import io.micrometer.observation.ObservationRegistry;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatusCode.valueOf;
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
				.GET("hello", handlers::hello)
				.path("/user", pathBuilder -> pathBuilder
						.GET("", handlers::user)
						.GET("/{id}", handlers::userById))
				.after((request, response) -> {
					var params = request.queryParams().toSingleValueMap();
					var pathWithQueryParam = request.queryParams().toSingleValueMap().keySet().stream().map(key -> String.format("%s=%s",key, params.get(key))).collect(Collectors.joining(","));
					if(response.statusCode().isError()) {
						log.error("{}?{} {}", request.requestPath(),pathWithQueryParam,response.statusCode().value());
					} else {
						log.info("{}?{} {}", request.requestPath(),pathWithQueryParam,response.statusCode().value());
					}

					return response;
				})
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

		var type = request.queryParam("type").orElse("");
		if(type.matches("^(FIRSTNAME|MIDDLENAME|LASTNAME)$")) {
			return ServerResponse.ok().body(greetingService.greeting(type), Greeting.class);
		} else {
			//throw new ResponseStatusException(HttpStatusCode.valueOf(400), "must match the type=FIRSTNAME|MIDDLENAME|LASTNAME");
			return ServerResponse.badRequest().body(
					Mono.error(new ResponseStatusException(valueOf(400), "must match the type=FIRSTNAME|MIDDLENAME|LASTNAME")),
					Mono.class);

		}
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
@RequiredArgsConstructor
class WebClientConfig {
	final WebClient.Builder builder;
	@Bean
	WebClient jsonPlaceHolderWebClient() {
		return builder
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
		var result = switch(name) {
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
				.flatMap(user -> {
					//call the needed service and fill the data
					return Mono.zip(
							todoService.byUserId(String.valueOf(user.id())).collectList(),
							postsService.byUserId(String.valueOf(user.id())).collectList(),
							albumService.byUserId(String.valueOf(user.id())).collectList()
					).map(zip -> Users.builder()
								.id(user.id())
								.address(user.address())
								.company(user.company())
								.email(user.email())
								.name(user.name())
								.phone(user.phone())
								.username(user.username())
								.website(user.website())
								.todos(zip.getT1())
								.posts(zip.getT2())
								.albums(zip.getT3())
								.build());
				})
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
		log.info("PostsService.byUserId({})", id);
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
		log.info("AlbumService.byUserId({})",id);
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
		log.info("TodoService.byUserId({})", id);
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