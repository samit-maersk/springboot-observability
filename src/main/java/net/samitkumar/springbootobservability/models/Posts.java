package net.samitkumar.springbootobservability.models;

import lombok.Builder;

public record Posts(int userId, int id, String title, String body) {
    @Builder
    public Posts {
    }
}
