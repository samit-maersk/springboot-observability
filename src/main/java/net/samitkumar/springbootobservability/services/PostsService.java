package net.samitkumar.springbootobservability.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.samitkumar.springbootobservability.models.Posts;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

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
