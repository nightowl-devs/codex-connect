package dev.nightowl.codexconnect.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder

public class Reasoning {
    private String title;
    private String content;
    private String id;
}
