package net.samitkumar.springbootobservability.models;

import lombok.Builder;

public record Comments(int postId, int id, String name, String email, String body) {
    @Builder
    public Comments {
    }
}
