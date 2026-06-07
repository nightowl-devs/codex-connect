package dev.nightowl.codexconnect.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nightowl.codexconnect.exception.CodexAuthException;
import dev.nightowl.codexconnect.model.auth.DeviceAuthResponse;
import dev.nightowl.codexconnect.model.auth.TokenResponse;
import lombok.Data;
import okhttp3.*;

import java.io.IOException;
import java.util.Objects;

public class DeviceAuthFlow {
    private static final String CLIENT_ID = "app_EMoamEEZ73f0CkXaXp7hrann";
    private static final String ISSUER = "https://auth.openai.com";
    private static final String USER_AGENT = "codex-connect/1.0";

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DeviceAuthFlow(OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public DeviceAuthInfo startDeviceAuth() {
        RequestBody body = RequestBody.create(
                "{\"client_id\":\"" + CLIENT_ID + "\"}",
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(ISSUER + "/api/accounts/deviceauth/usercode")
                .post(body)
                .header("Content-Type", "application/json")
                .header("User-Agent", USER_AGENT)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new CodexAuthException("Device auth initiation failed: " + response.code());
            }

            DeviceAuthResponse deviceAuth = objectMapper.readValue(
                    response.body().string(),
                    DeviceAuthResponse.class
            );

            int intervalMs = Math.max(
                    Integer.parseInt(deviceAuth.getInterval() != null ? deviceAuth.getInterval() : "5"),
                    1
            ) * 1000 + 3000;

            return new DeviceAuthInfo(deviceAuth, intervalMs);
        } catch (IOException e) {
            throw new CodexAuthException("Device auth initiation failed", e);
        }
    }

    public TokenResponse pollForToken(DeviceAuthResponse deviceAuth) {
        String requestBody = String.format(
                "{\"device_auth_id\":\"%s\",\"user_code\":\"%s\"}",
                deviceAuth.getDeviceAuthId(),
                deviceAuth.getUserCode()
        );

        RequestBody body = RequestBody.create(
                requestBody,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(ISSUER + "/api/accounts/deviceauth/token")
                .post(body)
                .header("Content-Type", "application/json")
                .header("User-Agent", USER_AGENT)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.code() == 403 || response.code() == 404) {
                return null;
            }

            if (!response.isSuccessful()) {
                throw new CodexAuthException("Device token polling failed: " + response.code());
            }

            String responseBody = response.body().string();
            DeviceTokenResponse tokenData = objectMapper.readValue(responseBody, DeviceTokenResponse.class);
            if (Objects.equals(tokenData.status, "success")) {
                return exchangeDeviceCode(tokenData);
            } else {
                throw new CodexAuthException("Device token polling failed: " + tokenData.status);
            }

        } catch (IOException e) {
            throw new CodexAuthException("Device token polling failed", e);
        }
    }

    private TokenResponse exchangeDeviceCode(DeviceTokenResponse tokenData) {
        FormBody body = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", tokenData.getAuthorizationCode())
                .add("redirect_uri", ISSUER + "/deviceauth/callback")
                .add("client_id", CLIENT_ID)
                .add("code_verifier", tokenData.getCodeVerifier())
                .build();

        Request request = new Request.Builder()
                .url(ISSUER + "/oauth/token")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new CodexAuthException("Device code exchange failed: " + response.code());
            }

            return objectMapper.readValue(response.body().string(), TokenResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CodexAuthException("Device code exchange failed", e);
        }
    }

    public record DeviceAuthInfo(DeviceAuthResponse response, int pollingIntervalMs) {
    }

    @Data
    private static class DeviceTokenResponse {
        private String status;
        @JsonProperty("authorization_code")
        private String authorizationCode;
        @JsonProperty("code_verifier")
        private String codeVerifier;
        @JsonProperty("code_challenge")
        private String codeChallenge;
        @JsonProperty("user_code_expiration")
        private String userCodeExpiration;
        @JsonProperty("user_code")
        private String userCode;


    }
}
