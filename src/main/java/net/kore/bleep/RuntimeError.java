package net.kore.bleep;

public class RuntimeError extends RuntimeException {
    protected Token token;
    protected int line;

    protected RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }

    protected RuntimeError(Token token, String message, Throwable cause) {
        super(message, cause);
        this.token = token;
    }

    protected RuntimeError(int line, String message) {
        super(message);
        this.line = line;
    }
}
