package dev.nightowl.codexconnect.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nightowl.codexconnect.auth.CodexAuthenticator;
import dev.nightowl.codexconnect.exception.CodexApiException;
import dev.nightowl.codexconnect.model.request.ChatRequest;
import dev.nightowl.codexconnect.model.response.ChatResponse;
import dev.nightowl.codexconnect.model.response.ModelsResponse;
import dev.nightowl.codexconnect.model.response.StreamEvent;
import dev.nightowl.codexconnect.model.response.UsageResponse;
import dev.nightowl.codexconnect.util.SseParser;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class CodexClient {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String RESPONSES_ENDPOINT = "/codex/responses";

    private final CodexAuthenticator authenticator;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final CodexClientConfig config;
    private final SseParser sseParser;
    private ModelsResponse modelsResponseCache;

    public CodexClient(CodexAuthenticator authenticator, CodexClientConfig config) {
        this.authenticator = authenticator;
        this.config = config;
        this.httpClient = config.buildHttpClient();
        this.objectMapper = config.buildObjectMapper();
        this.sseParser = new SseParser(objectMapper);
    }


    public ModelsResponse getAvailableModels() {
        ModelsResponse modelsResponse = getAllModels();
        String plan = authenticator.getAccountInfo().getPlanType();
        return ModelsResponse.builder().models(
                modelsResponse.getModels().stream().filter(model -> model.getAvailableInPlans() == null || model.getAvailableInPlans().contains(plan)).toList()
        ).build();
    }

    public ModelsResponse getAllModels() {
        if (modelsResponseCache != null) {
            return modelsResponseCache;
        }
        String accessToken = authenticator.getAccessToken();

        Request request = new Request.Builder()
                .url(config.getApiBase() + "/codex/models?client_version=%s".formatted(config.getClientVersion()))
                .get()
                .header("Authorization", "Bearer " + accessToken)
                .header("originator", config.getOriginator())
                .header("chatgpt-account-id", authenticator.getAccountInfo().getAccountId())
                .header("User-Agent", "codex-tui/%s (Windows 10.0.28020; x86_64) unknown (codex-tui; %s)".formatted(config.getClientVersion(), config.getClientVersion()))
                .header("version", config.getClientVersion())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody;
                try {
                    errorBody = response.body().string();
                } catch (Exception e) {
                    errorBody = "Could not read error body";
                }
                throw new CodexApiException(
                        "API request failed: " + response.code() + " - " + errorBody,
                        response.code()
                );
            }
            modelsResponseCache = objectMapper.readValue(response.body().string(), ModelsResponse.class);
            return modelsResponseCache;
        } catch (IOException e) {

            throw new CodexApiException("Failed to get models", 0, e);
        }


    }

    public ChatResponse sendMessage(ChatRequest request) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ChatResponse> responseRef = new AtomicReference<>();
        AtomicReference<Exception> errorRef = new AtomicReference<>();

        sendMessageStream(request, StreamingHandler.createCollectingHandler(
                // System.out::println,
                null,
                response -> {
                    responseRef.set(response);
                    latch.countDown();
                }
        ), error -> {
            errorRef.set(error);
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CodexApiException("Request interrupted", 0, e);
        }

        if (errorRef.get() != null) {
            throw new CodexApiException("Request failed", 0, errorRef.get());
        }

        return responseRef.get();
    }

    public UsageResponse getUsage() {
        String accessToken = authenticator.getAccessToken();

        Request request = new Request.Builder()
                .url(config.getApiBase() + "/wham/usage")
                .get()
                .header("Authorization", "Bearer " + accessToken)
                .header("originator", config.getOriginator())
                .header("chatgpt-account-id", authenticator.getAccountInfo().getAccountId())
                .header("User-Agent", "codex-tui/%s (Windows 10.0.28020; x86_64) unknown (codex-tui; %s)".formatted(config.getClientVersion(), config.getClientVersion()))
                .header("version", config.getClientVersion())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody;
                try {
                    errorBody = response.body().string();
                } catch (Exception e) {
                    errorBody = "Could not read error body";
                }
                throw new CodexApiException(
                        "API request failed: " + response.code() + " - " + errorBody,
                        response.code()
                );
            }

            return objectMapper.readValue(response.body().string(), UsageResponse.class);
        } catch (IOException e) {

            throw new CodexApiException("Failed to get usage", 0, e);
        }
    }
    //019ea2bb-96d4-7151-b13b-4b960ac695dd
private String CHARS = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private void generateRequestId() {

    }
    public void sendMessageStream(ChatRequest request, Consumer<StreamEvent> onEvent, Consumer<Exception> onError) {
        try {
            ChatRequest enrichedRequest = enrichRequest(request);
            String json = objectMapper.writeValueAsString(enrichedRequest);
            RequestBody body = RequestBody.create(json, JSON);


            String accessToken = authenticator.getAccessToken();


            String ourId = UUID.randomUUID().toString();
            String sessionId = ourId;
            String threadId = ourId;
            String requestId = threadId;
            String windowId = threadId + ":0";
            String turnMetadata = "{\"session_id\":\"%s\",\"thread_id\":\"%s\",\"thread_source\":\"user\",\"turn_id\":\"\",\"sandbox\":\"windows_elevated\",\"request_kind\":\"prewarm\",\"window_id\":\"%s:0\"}".formatted(sessionId, threadId, threadId);
//based on the codex clients code
            Request.Builder requestBuilder = new Request.Builder()
                    .url(config.getApiBase() + RESPONSES_ENDPOINT)
                    .post(body)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .header("originator", config.getOriginator())
                    .header("chatgpt-account-id", authenticator.getAccountInfo().getAccountId())
                    .header("version", config.getClientVersion())
                    .header("User-Agent", "codex-tui/%s (Windows 10.0.28020; x86_64) unknown (codex-tui; %s)".formatted(config.getClientVersion(), config.getClientVersion()))
                    .header("session-id", sessionId)
                    .header("thread-id", threadId)
                    .header("x-client-request-id", requestId)
                    .header("x-codex-window-id", windowId)
                    .header("x-codex-beta-features", "terminal_resize_reflow,external_migration,prevent_idle_sleep")
                    .header("x-codex-turn-metadata", turnMetadata);
            Request httpRequest = requestBuilder.build();

            httpClient.newCall(httpRequest).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    if (onError != null) {
                        onError.accept(e);
                    }
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    if (!response.isSuccessful()) {
                        String errorBody;
                        try {
                            errorBody = response.body().string();
                        } catch (Exception e) {
                            errorBody = "Could not read error body";
                        }

                        if (onError != null) {
                            onError.accept(new CodexApiException(
                                    "API request failed: " + response.code() + " - " + errorBody,
                                    response.code()
                            ));
                        }
                        response.close();
                        return;
                    }

                    try (response) {
                        sseParser.parse(response.body().byteStream(), new SseParser.EventHandler() {
                            @Override
                            public void onEvent(StreamEvent event) {
                                if (onEvent != null) {
                                    onEvent.accept(event);
                                }
                            }

                            @Override
                            public void onComplete() {
                            }

                            @Override
                            public void onError(Exception e) {
                                if (onError != null) {
                                    onError.accept(e);
                                }
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            if (onError != null) {
                onError.accept(e);
            }
        }
    }

    private ChatRequest enrichRequest(ChatRequest request) {
        ChatRequest.ChatRequestBuilder builder = ChatRequest.builder()
                .model(request.getModel())
                .store(false) //never uses data for trainin
                .stream(true)
                .instructions(request.getInstructions())
                .input(request.getInput())
                .tools(request.getTools())
                .reasoning(request.getReasoning())
                .textConfig(request.getTextConfig())
                .promptCacheKey(request.getPromptCacheKey());

        if (request.getInclude() != null) {
            builder.include(request.getInclude());
        } else {
            builder.include(List.of("reasoning.encrypted_content"));
        }

        return builder.build();
    }
}
