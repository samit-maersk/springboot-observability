package net.samitkumar.springbootobservability.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.samitkumar.springbootobservability.models.Users;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
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
                    log.error("UserService.allUser {}", e.getMessage());
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
