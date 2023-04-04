package net.samitkumar.springbootobservability.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.samitkumar.springbootobservability.models.Todos;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

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
