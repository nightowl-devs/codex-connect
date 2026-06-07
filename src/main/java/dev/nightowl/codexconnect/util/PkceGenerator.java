package dev.nightowl.codexconnect.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PkceGenerator {
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~";
    private static final int VERIFIER_LENGTH = 43;

    public static PkceCodes generate() {
        SecureRandom random = new SecureRandom();
        StringBuilder verifier = new StringBuilder(VERIFIER_LENGTH);

        for (int i = 0; i < VERIFIER_LENGTH; i++) {
            verifier.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }

        String verifierString = verifier.toString();
        String challenge = generateChallenge(verifierString);

        return new PkceCodes(verifierString, challenge);
    }

    private static String generateChallenge(String verifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(verifier.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private static String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    public record PkceCodes(String verifier, String challenge) {
    }
}
