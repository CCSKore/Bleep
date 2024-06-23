package net.kore.bleep;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class Bleep {
    protected static boolean hadError = false;
    protected static boolean hadRuntimeError = false;
    protected static Parser parser = null;

    public static Environment run(File source) {
        try {
            return run(Files.readString(source.toPath(), Charset.defaultCharset()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Environment run(File source, Interpreter interpreter) {
        try {
            return run(Files.readString(source.toPath(), Charset.defaultCharset()), interpreter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Environment run(String source) {
        return run(source, new Interpreter());
    }

    public static Environment run(String source, Interpreter interpreter) {
        Interpreter.canReplace = false;
        Interpreter.INSTANCE = interpreter;
        Scanner scanner = new Scanner(source);

        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if (hadError) return null;

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        if (hadError) return null;

        Parser previousParser = Bleep.parser;
        Bleep.parser = parser;
        interpreter.interpret(statements);
        Bleep.parser = previousParser;
        return interpreter.globals;
    }

    protected static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[Line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    protected static void error(Token token, String message) {
        if (token.type == TokenType.EOF) report(token.line, " at end", message);
        else report(token.line, " at '" + token.lexeme + "'", message);
    }

    protected static void runtimeError(RuntimeError error) {
        if (error.token != null) {
            BleepAPI.logProvider.raw(Colors.TEXT_BRIGHT_RED + "[Line " + error.token.line + "] " +
                    error.getMessage() + Colors.TEXT_RESET);
        } else {
            BleepAPI.logProvider.raw(Colors.TEXT_BRIGHT_RED + "[Line " + parser.maybeErrorPoint.line + "] " +
                    error.getMessage() + Colors.TEXT_RESET);
        }
        hadRuntimeError = true;
    }
}