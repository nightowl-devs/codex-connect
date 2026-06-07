package dev.nightowl.codexconnect.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import okhttp3.OkHttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Data
@Builder
public class CodexClientConfig {
    private static final String DEFAULT_API_BASE = "https://chatgpt.com/backend-api";
    private static final String DEFAULT_ORIGINATOR = "codex-tui";

    @Builder.Default
    private String apiBase = DEFAULT_API_BASE;

    @Builder.Default
    private String clientVersion = "0.137.0";

    @Builder.Default
    private String originator = DEFAULT_ORIGINATOR;

    @Builder.Default
    private Duration readTimeout = Duration.ofSeconds(60);

    @Builder.Default
    private Duration connectTimeout = Duration.ofSeconds(30);

    @Builder.Default
    private Duration writeTimeout = Duration.ofSeconds(30);

    private OkHttpClient customHttpClient;

    private ObjectMapper customObjectMapper;

    public OkHttpClient buildHttpClient() {
        if (customHttpClient != null) {
            return customHttpClient;
        }

        return new OkHttpClient.Builder()
                .readTimeout(readTimeout.toMillis(), TimeUnit.MILLISECONDS)
                .connectTimeout(connectTimeout.toMillis(), TimeUnit.MILLISECONDS)
                .writeTimeout(writeTimeout.toMillis(), TimeUnit.MILLISECONDS)
                .build();
    }

    public ObjectMapper buildObjectMapper() {
        if (customObjectMapper != null) {
            return customObjectMapper;
        }
        return new ObjectMapper();
    }
}
