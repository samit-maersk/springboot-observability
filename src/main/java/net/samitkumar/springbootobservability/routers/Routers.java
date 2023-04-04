package net.samitkumar.springbootobservability.routers;

import lombok.extern.slf4j.Slf4j;
import net.samitkumar.springbootobservability.handlers.Handlers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.stream.Collectors;

@Configuration
@Slf4j
public class Routers {
    @Bean
    public RouterFunction route(Handlers handlers) {
        return RouterFunctions
                .route()
                .GET("hello", handlers::hello)
                .path("/user", pathBuilder -> pathBuilder
                        .GET("", handlers::user)
                        .GET("/{id}", handlers::userById))
                .after((request, response) -> logReqRes(request, response))
                .build();
    }

    private ServerResponse logReqRes(ServerRequest request, ServerResponse response) {
        var params = request.queryParams().toSingleValueMap();
        var pathWithQueryParam = request.queryParams().toSingleValueMap().keySet().stream().map(key -> String.format("%s=%s",key, params.get(key))).collect(Collectors.joining(","));
        if(response.statusCode().isError()) {
            log.error("{}?{} {}", request.requestPath(),pathWithQueryParam, response.statusCode().value());
        } else {
            log.info("{}?{} {}", request.requestPath(),pathWithQueryParam, response.statusCode().value());
        }

        return response;
    }
}
