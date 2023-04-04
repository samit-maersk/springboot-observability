package net.samitkumar.springbootobservability.models;

import lombok.Builder;

import java.util.List;

public record Albums(int userId, int id, String title, List<Photos> photos) {
    @Builder
    public Albums {
    }
}
