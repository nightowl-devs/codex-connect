package dev.nightowl.codexconnect.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import dev.nightowl.codexconnect.model.response.StreamEvent;

import java.io.IOException;

public class DeltaDeserializer extends JsonDeserializer<StreamEvent.Delta> {
    @Override
    public StreamEvent.Delta deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        StreamEvent.Delta delta = new StreamEvent.Delta();

        if (node.isTextual()) {
            delta.setText(node.asText());
        } else if (node.isObject()) {
            if (node.has("type")) {
                delta.setType(node.get("type").asText());
            }
            if (node.has("role")) {
                delta.setRole(node.get("role").asText());
            }
            if (node.has("text")) {
                delta.setText(node.get("text").asText());
            }
        }

        return delta;
    }
}
