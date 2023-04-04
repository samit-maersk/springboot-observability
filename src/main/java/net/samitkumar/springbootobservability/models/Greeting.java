package net.samitkumar.springbootobservability.models;

import lombok.Builder;

public record Greeting(String name) {
    @Builder
    public Greeting {
    }
}
