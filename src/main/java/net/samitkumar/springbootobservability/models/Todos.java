package net.samitkumar.springbootobservability.models;

import lombok.Builder;

public record Todos(int userId, int id, String title, String completed) {
    @Builder
    public Todos {
    }
}
