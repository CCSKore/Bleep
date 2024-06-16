package net.kore.bleep;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class Bleep {
    private static final Interpreter interpreter = new Interpreter();
    protected static boolean hadError = false;
    protected static boolean hadRuntimeError = false;

    public static Environment run(File source) {
        try {
            return run(Files.readString(source.toPath(), Charset.defaultCharset()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Environment run(String source) {
        Scanner scanner = new Scanner(source);

        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if (hadError) return null;

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        if (hadError) return null;

        interpreter.interpret(statements);
        return interpreter.globals;
    }

    protected static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    protected static void error(Token token, String message) {
        if (token.type == TokenType.EOF) report(token.line, " at end", message);
        else report(token.line, " at '" + token.lexeme + "'", message);
    }

    protected static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
            "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}