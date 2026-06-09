package dev.nightowl.codexconnect.auth;

import dev.nightowl.codexconnect.model.auth.TokenResponse;
import lombok.Getter;

import java.time.Instant;

public class TokenManager {
    private final TokenRefresher refresher;
    @Getter
    private String accessToken;
    private String refreshToken;
    @Getter
    private String idToken;
    private Instant expiresAt;

    public TokenManager(TokenRefresher refresher) {
        this.refresher = refresher;
    }

    public synchronized void setTokens(TokenResponse tokenResponse) {
        this.accessToken = tokenResponse.getAccessToken();
        this.refreshToken = tokenResponse.getRefreshToken();
        this.idToken = tokenResponse.getIdToken();

        if (tokenResponse.getExpiresIn() != null) {
            this.expiresAt = Instant.now().plusSeconds(tokenResponse.getExpiresIn());
        } else {
            this.expiresAt = Instant.now().plusSeconds(3600);
        }
    }

    public synchronized String getValidAccessToken() {
        if (accessToken == null) {
            throw new IllegalStateException("No access token available");
        }

        if (expiresAt != null && Instant.now().isAfter(expiresAt.minusSeconds(60))) {

            refreshAccessToken();
        }

        return accessToken;
    }

    private void refreshAccessToken() {
        if (refreshToken == null) {
            throw new IllegalStateException("No refresh token available");
        }

        TokenResponse newTokens = refresher.refresh(refreshToken);
        setTokens(newTokens);
    }

    public synchronized boolean hasTokens() {
        return accessToken != null;
    }

    public interface TokenRefresher {
        TokenResponse refresh(String refreshToken);
    }
}
