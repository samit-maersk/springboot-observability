package net.samitkumar.springbootobservability.models;

import lombok.Builder;

import java.util.List;

public record Photos(int albumId, int id, String title, String url, String thumbnailUrl, List<Comments> comments) {
    @Builder
    public Photos {
    }
}
