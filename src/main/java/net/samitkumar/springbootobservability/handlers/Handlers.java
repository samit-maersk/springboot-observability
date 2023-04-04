package net.samitkumar.springbootobservability.handlers;

import lombok.RequiredArgsConstructor;
import net.samitkumar.springbootobservability.models.Greeting;
import net.samitkumar.springbootobservability.models.Users;
import net.samitkumar.springbootobservability.services.GreetingService;
import net.samitkumar.springbootobservability.services.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatusCode.valueOf;

@Configuration
@RequiredArgsConstructor
public class Handlers {
    final GreetingService greetingService;
    final UserService userService;

    public Mono<ServerResponse> hello(ServerRequest request) {

        var type = request.queryParam("type").orElse("");
        if (type.matches("^(FIRSTNAME|MIDDLENAME|LASTNAME)$")) {
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
