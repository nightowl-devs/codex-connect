package dev.nightowl.codexconnect.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import dev.nightowl.codexconnect.auth.BrowserAuthFlow;
import dev.nightowl.codexconnect.auth.CodexAuthenticator;
import dev.nightowl.codexconnect.client.CodexClient;
import dev.nightowl.codexconnect.client.CodexClientConfig;
import dev.nightowl.codexconnect.client.StreamingHandler;
import dev.nightowl.codexconnect.model.auth.TokenResponse;
import dev.nightowl.codexconnect.model.request.ChatRequest;
import dev.nightowl.codexconnect.model.request.Message;
import dev.nightowl.codexconnect.model.request.ReasoningConfig;
import dev.nightowl.codexconnect.model.response.ModelsResponse;
import okhttp3.OkHttpClient;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    private static final int OAUTH_PORT = 1455;

    public static void main(String[] args) {

        try {
            OkHttpClient httpClient = new OkHttpClient();
            ObjectMapper objectMapper = new ObjectMapper();

            CodexAuthenticator authenticator = new CodexAuthenticator(httpClient, objectMapper);

            File saveFile = new File("tokens.json");
            TokenResponse tokens;
            if (saveFile.exists()) {
                tokens = objectMapper.readValue(saveFile, TokenResponse.class);

            } else {
                BrowserAuthFlow authFlow = authenticator.getBrowserAuthFlow();

                BrowserAuthFlow.AuthorizationRequest authRequest = authFlow.startAuthFlow();

                System.out.println("URL: " + authRequest.authorizationUrl());

                CountDownLatch latch = new CountDownLatch(1);
                AtomicReference<String> authCodeRef = new AtomicReference<>();
                AtomicReference<String> stateRef = new AtomicReference<>();

                HttpServer server = HttpServer.create(new InetSocketAddress(OAUTH_PORT), 0);
                server.createContext("/auth/callback", exchange -> {
                    String query = exchange.getRequestURI().getQuery();
                    String code = extractParam(query, "code");
                    String state = extractParam(query, "state");
                    String error = extractParam(query, "error");

                    if (error != null) {
                        String response = " bad: " + error;
                        exchange.getResponseHeaders().add("Content-Type", "text/html");
                        exchange.sendResponseHeaders(400, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                        latch.countDown();
                        return;
                    }

                    if (code != null && state != null) {
                        authCodeRef.set(code);
                        stateRef.set(state);

                        String response = "yay";
                        exchange.getResponseHeaders().add("Content-Type", "text/html");
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();

                        latch.countDown();
                    }
                });

                server.start();

                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(authRequest.authorizationUrl()));
                } else {
                    System.out.println(authRequest.authorizationUrl());
                }

                latch.await();
                server.stop(0);


                tokens = authFlow.exchangeCode(authCodeRef.get(), authRequest.pkce());
                if (!saveFile.exists()) {
                    saveFile.createNewFile();
                }

                FileWriter writer = new FileWriter(saveFile);
                writer.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tokens));
                writer.close();
            }


            authenticator.setTokens(tokens);
//
//            DeviceAuthFlow  authFlow = authenticator.getDeviceAuthFlow();
//            DeviceAuthFlow.DeviceAuthInfo info = authFlow.startDeviceAuth();
//            System.out.println("Code: " + info.getResponse().getUserCode());
//            System.out.println(info.getResponse());
//            TokenResponse tkn = null;
//            System.out.println("https://auth.openai.com/codex/device");
//            while (tkn == null) {
//                tkn = authFlow.pollForToken(info.getResponse());
//                Thread.sleep(info.getPollingIntervalMs());
//
//            }
//            authenticator.setTokens(tkn);

            System.out.println(authenticator.getAccountInfo());

            CodexClientConfig config = CodexClientConfig.builder().build();
            CodexClient client = new CodexClient(authenticator, config);
            System.out.println(client.getUsage());
            ModelsResponse modelsResponse = client.getAvailableModels();
            modelsResponse.getModels().forEach(model -> {
                System.out.println("Model: " + model.getSlug());
                System.out.println("  Max Tokens: " + model.getContextWindow());
                System.out.println("  Description: " + model.getDescription());
                System.out.println("  Reasoning: ");
                model.getSupportedReasoningLevels().forEach(level ->
                {
                    System.out.println("    " + level.getEffort() + ": " + level.getDescription());
                });

            });

            System.out.println("\n\n\nresponse:\n\n");
            //controll everything yourself
//            client.sendMessageStream(ChatRequest.builder()
//                    .model("gpt-5.4-mini")
//                    .instructions("you are a helpful assistant whose name is botwinka")
//                    .reasoning(ReasoningConfig.none())
//                    .input(List.of((Message.builder().content("Write a poem about your name?").role("user").build()))).build(),
//                    streamEvent -> {
//                        if (streamEvent.getDelta() != null) {
//                            System.out.print(streamEvent.getDelta().getText());
//                        }
//                    },null);


            //or await the respone and collcet it using tthe streaming handler

//            ChatResponse response = client.sendMessage(ChatRequest.builder()
//                    .model("gpt-5.4-mini")
//                    .instructions("you are a helpful assistant whose name is botwinka")
//                    .reasoning(ReasoningConfig.medium())
//                    .input(List.of((Message.builder().content("Write a poem about your name?").role("user").build()))).build());
//
//            response.getMessages().stream().filter(m -> "assistant".equals(m.getRole())).forEach(m -> {
//
//                System.out.println(m.getContent());
//            });
            //recommended approach for live appliactions would be to use both:
            //controll everything yourself
            client.sendMessageStream(ChatRequest.builder()
                            .model("gpt-5.4-mini")
                            .instructions("you are a helpful assistant whose name is botwinka, when thinkng the first line fo your thinking is allways **SUBJECT (3-5 words** followed by a  newline")
                            .reasoning(ReasoningConfig.high())
                            .input(List.of((Message.builder().content("what is your inner emotion right now  think deeply").role("user").build()))).build(),
                    StreamingHandler.createCollectingHandler(
                            System.out::println,
                            resp -> {
                                System.out.println("\n\n\nFinal response received:");
                                System.out.println(resp);
                                System.out.println("status: " + resp.getStatus());
                                if (resp.getUsage() != null) {
                                    System.out.println("used tokens: " + resp.getUsage().getTotalTokens());
                                }
                            }

                    ),
                    null);


        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String extractParam(String query, String param) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(param)) {
                return kv[1];
            }
        }
        return null;
    }


}