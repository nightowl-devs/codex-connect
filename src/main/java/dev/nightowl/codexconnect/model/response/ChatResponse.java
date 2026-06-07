package dev.nightowl.codexconnect.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatResponse {
    private String id;
    private String status;
    private Reasoning reasoning;
    private List<MessageContent> messages;
    private List<ToolCall> toolCalls;
    private ResponseTokenUsage usage;

    @Data
    @Builder
    public static class MessageContent {
        private String role;
        private String content;
    }
}
