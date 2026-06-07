package dev.nightowl.codexconnect.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nightowl.codexconnect.exception.CodexAuthException;
import dev.nightowl.codexconnect.model.auth.TokenResponse;
import dev.nightowl.codexconnect.util.PkceGenerator;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class BrowserAuthFlow {
    private static final String CLIENT_ID = "app_EMoamEEZ73f0CkXaXp7hrann";
    private static final String ISSUER = "https://auth.openai.com";
    private static final String REDIRECT_URI = "http://localhost:1455/auth/callback";
    private static final String SCOPE = "openid profile email offline_access";

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BrowserAuthFlow(OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public AuthorizationRequest startAuthFlow() {
        PkceGenerator.PkceCodes pkce = PkceGenerator.generate();
        String state = generateState();
        String authUrl = buildAuthorizationUrl(pkce, state);

        return new AuthorizationRequest(authUrl, state, pkce);
    }

    private String buildAuthorizationUrl(PkceGenerator.PkceCodes pkce, String state) {
        String url = ISSUER +
                "/oauth/authorize?" +
                "response_type=code" +
                "&client_id=" + encode(CLIENT_ID) +
                "&redirect_uri=" + encode(REDIRECT_URI) +
                "&scope=" + encode(SCOPE) +
                "&code_challenge=" + encode(pkce.challenge()) +
                "&code_challenge_method=S256" +
                "&id_token_add_organizations=true" +
                "&codex_cli_simplified_flow=true" +
                "&state=" + encode(state) +
                "&originator=opencode";

        return url;
    }

    public TokenResponse exchangeCode(String code, PkceGenerator.PkceCodes pkce) {
        FormBody body = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", REDIRECT_URI)
                .add("client_id", CLIENT_ID)
                .add("code_verifier", pkce.verifier())
                .build();

        Request request = new Request.Builder()
                .url(ISSUER + "/oauth/token")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new CodexAuthException("Token exchange failed: " + response.code());
            }

            return objectMapper.readValue(response.body().string(), TokenResponse.class);
        } catch (IOException e) {
            throw new CodexAuthException("Token exchange failed", e);
        }
    }

    private String generateState() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public record AuthorizationRequest(String authorizationUrl, String state, PkceGenerator.PkceCodes pkce) {
    }
}
