package net.kore.bleep;

import java.util.List;

public class Bleep {
    private static final Interpreter interpreter = new Interpreter();
    protected static boolean hadError = false;
    protected static boolean hadRuntimeError = false;

    public static void run(String source) {
        Scanner scanner = new Scanner(source);

        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if (hadError) return;

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        if (hadError) return;

        interpreter.interpret(statements);
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