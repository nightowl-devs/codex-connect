package dev.nightowl.codexconnect.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nightowl.codexconnect.exception.CodexAuthException;
import dev.nightowl.codexconnect.model.auth.AccountInfo;
import dev.nightowl.codexconnect.model.auth.TokenResponse;
import dev.nightowl.codexconnect.util.JwtParser;
import lombok.Getter;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class CodexAuthenticator {
    private static final String ISSUER = "https://auth.openai.com";
    private static final String CLIENT_ID = "app_EMoamEEZ73f0CkXaXp7hrann";

    private final TokenManager tokenManager;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    @Getter
    private final BrowserAuthFlow browserAuthFlow;
    @Getter
    private final DeviceAuthFlow deviceAuthFlow;

    public CodexAuthenticator(OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.tokenManager = new TokenManager(this::refreshToken);
        this.browserAuthFlow = new BrowserAuthFlow(httpClient, objectMapper);
        this.deviceAuthFlow = new DeviceAuthFlow(httpClient, objectMapper);
    }

    public void setTokens(TokenResponse tokenResponse) {
        tokenManager.setTokens(tokenResponse);
    }

    public String getAccessToken() {
        return tokenManager.getValidAccessToken();
    }

    public AccountInfo getAccountInfo() {
        String accessToken = tokenManager.getAccessToken();
        if (accessToken == null) {
            return null;
        }


        return JwtParser.extractAccountInfo(accessToken);
    }

    private String getIdToken() {
        return tokenManager.getAccessToken();
    }

    public boolean isAuthenticated() {
        return tokenManager.hasTokens();
    }

    private TokenResponse refreshToken(String refreshToken) {
        FormBody body = new FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .add("client_id", CLIENT_ID)
                .build();

        Request request = new Request.Builder()
                .url(ISSUER + "/oauth/token")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new CodexAuthException("Token refresh failed: " + response.code());
            }

            return objectMapper.readValue(response.body().string(), TokenResponse.class);
        } catch (IOException e) {
            throw new CodexAuthException("Token refresh failed", e);
        }
    }
}
