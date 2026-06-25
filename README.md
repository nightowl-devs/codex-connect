# codex-connect

Unofficial Java  client for OpenAI's Codex API.

## Install


```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.nightowl-devs:codex-connect:v1.0.3")
}
```

If you are using another build tool, use the same JitPack coordinates and add `https://jitpack.io` as a repository.

## Quick Start

The library is centered around two pieces:

- `CodexAuthenticator` for logging in and managing tokens
- `CodexClient` for usage, models, and chat requests

Example:

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

## Auth Flow

There are two auth paths in the repo:

- `BrowserAuthFlow`, which opens the OpenAI auth URL and finishes the PKCE exchange through a local callback on `http://localhost:1455/auth/callback`
- `DeviceAuthFlow`, which uses OpenAI device auth endpoints and polls until the token is ready


## What It Can Do

- Sign in with an ChatGPT account
- Refresh access tokens automatically
- Read account info from the JWT
- Fetch usage data from `/wham/usage`
- Fetch all Codex models and filter them to the plan the account can use
- Send chat requests to `/codex/responses` and stream SSE events

## Requirements

- Java 17
- Gradle
- Internet access to OpenAI auth and Codex endpoints
- A valid ChatGPT/OpenAI account
- If you use the browser auth flow, port `1455` must be available locally AND IT MUST be because its validated server side on openai's side

## Demo

Exmaple in `src/test/java/dev/nightowl/codexconnect/test/Main.java`