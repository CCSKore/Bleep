package net.kore.bleep;

public class RuntimeError extends RuntimeException {
    protected final Token token;

    protected RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }

    protected RuntimeError(Token token, String message, Throwable cause) {
        super(message, cause);
        this.token = token;
    }
}
