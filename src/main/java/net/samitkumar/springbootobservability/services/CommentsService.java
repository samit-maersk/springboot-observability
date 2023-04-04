package net.samitkumar.springbootobservability.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
class CommentsService {
    final WebClient jsonPlaceHolderWebClient;
}
