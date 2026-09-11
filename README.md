# codex-connect

Unofficial Java client for OpenAI's Codex API.

## Install


```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.nightowl-devs:codex-connect:v1.0.5")
}
```

[Watch Showcase...](https://raw.githubusercontent.com/nightowl-devs/codex-connect/refs/heads/main/showcase.mp4)

## Quick Start


```java
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nightowl.codexconnect.auth.CodexAuthenticator;
import dev.nightowl.codexconnect.client.CodexClient;
import dev.nightowl.codexconnect.client.CodexClientConfig;
import dev.nightowl.codexconnect.model.request.ChatRequest;
import dev.nightowl.codexconnect.model.request.Message;
import dev.nightowl.codexconnect.model.request.ReasoningConfig;
import okhttp3.OkHttpClient;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        OkHttpClient httpClient = new OkHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();

        CodexAuthenticator authenticator = new CodexAuthenticator(httpClient, objectMapper);

        // load tokens from a file or get them from an authflow
        // authenticator.setTokens(tokens);

        CodexClient client = new CodexClient(authenticator, CodexClientConfig.builder().build());

        System.out.println(client.getUsage());
        System.out.println(client.getAvailableModels());

        client.sendMessageStream(
                ChatRequest.builder()
                        .model("gpt-5.4-mini")
                        .instructions("You are a helpful assistant.")
                        .reasoning(ReasoningConfig.high())
                        .input(List.of(
                                Message.builder()
                                        .role("user")
                                        .content("Write a short haiku about shipping a CLI tool.")
                                        .build()
                        ))
                        .build(),
                event -> {
                    if (event.getDelta() != null) {
                        System.out.print(event.getDelta().getText());
                    }
                },
                Throwable::printStackTrace
        );
    }
}
```


## Requirements

- Java 17
- Gradle
- Internet access to OpenAI auth and Codex endpoints
- A valid ChatGPT/OpenAI account
- If you use the browser auth flow, port `1455` must be available locally AND IT MUST be because its validated server side on openai's side

## Demo

Exmaple in `src/test/java/dev/nightowl/codexconnect/test/Main.java`
