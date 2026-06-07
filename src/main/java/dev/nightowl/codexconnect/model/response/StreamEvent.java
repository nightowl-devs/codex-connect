package dev.nightowl.codexconnect.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dev.nightowl.codexconnect.util.DeltaDeserializer;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StreamEvent {
    private String type;

    @JsonProperty("response")
    private Response response;

    @JsonProperty("delta")
    @JsonDeserialize(using = DeltaDeserializer.class)
    private Delta delta;

    @JsonProperty("item")
    private Item item;

    private ResponseTokenUsage usage;

    @JsonProperty("call_id")
    private String callId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("arguments")
    private String arguments;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private String id;
        private String status;
        private List<Item> output;
        private ResponseTokenUsage usage;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Delta {
        private String type;
        private String role;
        private String text;
        private Map<String, Object> content;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String id;
        private String type;
        private String role;
        private String status;
        private List<Content> content;

        @JsonProperty("call_id")
        private String callId;

        private String name;
        private String arguments;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
        private String type;
        private String text;
    }
}

