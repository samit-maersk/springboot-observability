package net.samitkumar.springbootobservability.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.samitkumar.springbootobservability.models.Albums;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
class AlbumService {
    final WebClient jsonPlaceHolderWebClient;

    public Flux<Albums> byUserId(String id) {
        log.info("AlbumService.byUserId({})", id);
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
