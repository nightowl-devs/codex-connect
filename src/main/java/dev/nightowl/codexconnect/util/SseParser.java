package dev.nightowl.codexconnect.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nightowl.codexconnect.model.response.StreamEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SseParser {
    private final ObjectMapper objectMapper;

    public SseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void parse(InputStream inputStream, EventHandler handler) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String jsonData = line.substring(6);

                    if ("[DONE]".equals(jsonData)) {
                        handler.onComplete();
                        break;
                    }

                    try {
                        StreamEvent event = objectMapper.readValue(jsonData, StreamEvent.class);
                        handler.onEvent(event);

                        if ("response.done".equals(event.getType()) ||
                                "response.completed".equals(event.getType())) {
                            handler.onComplete();
                            break;
                        }
                    } catch (Exception e) {
                        handler.onError(e);
                    }
                }
            }
        } catch (IOException e) {
            handler.onError(e);
        }
    }

    public interface EventHandler {
        void onEvent(StreamEvent event);

        void onComplete();

        void onError(Exception e);
    }
}
