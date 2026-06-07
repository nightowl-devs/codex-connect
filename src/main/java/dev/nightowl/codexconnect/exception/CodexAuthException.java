package dev.nightowl.codexconnect.exception;

public class CodexAuthException extends RuntimeException {
    public CodexAuthException(String message) {
        super(message);
    }

    public CodexAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
