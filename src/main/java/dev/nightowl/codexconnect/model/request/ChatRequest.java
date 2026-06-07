package dev.nightowl.codexconnect.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatRequest {
    private String model;

    @Builder.Default
    private Boolean store = false;

    @Builder.Default
    private Boolean stream = true;

    private String instructions;

    private List<Message> input;

    private List<Tool> tools;

    private ReasoningConfig reasoning;

    @JsonProperty("text")
    private TextConfig textConfig;

    private List<String> include;

    @JsonProperty("prompt_cache_key")
    private String promptCacheKey;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TextConfig {
        private String verbosity;
    }
}
