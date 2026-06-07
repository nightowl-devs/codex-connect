package dev.nightowl.codexconnect.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseTokenUsage {
    @JsonProperty("input_tokens")
    private Integer inputTokens;

    @JsonProperty("output_tokens")
    private Integer outputTokens;

    @JsonProperty("total_tokens")
    private Integer totalTokens;

    @JsonProperty("cache_read_input_tokens")
    private Integer cacheReadInputTokens;

    @JsonProperty("cache_creation_input_tokens")
    private Integer cacheCreationInputTokens;
}
