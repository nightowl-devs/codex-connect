package dev.nightowl.codexconnect.exception;

public class CodexApiException extends RuntimeException {
    private final int statusCode;

    public CodexApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public CodexApiException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
