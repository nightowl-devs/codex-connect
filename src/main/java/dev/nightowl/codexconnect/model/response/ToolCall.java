package dev.nightowl.codexconnect.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ToolCall {
    private String id;

    private String name;

    private String arguments;

    @JsonProperty("call_id")
    private String callId;
}
