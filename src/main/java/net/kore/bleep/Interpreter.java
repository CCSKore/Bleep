package net.kore.bleep;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    protected final Environment globals;
    private Environment environment;
    private final Map<Expr, Integer> locals = new HashMap<>();

    protected static Interpreter INSTANCE = null;
    protected static boolean canReplace = true;
    public static Interpreter get() {
        if (INSTANCE == null) {
            throw new RuntimeException("Interpreter not started.");
        }
        return INSTANCE;
    }

    protected Interpreter() {
        if (canReplace) { // We only want access to the first Interpreter
            canReplace = false;
            INSTANCE = this;
        }
        globals = new Environment();
        environment = globals;
        globals.define("clock", new BleepCallable() {
            @Override
            public int arity(List<Object> arguments) { return 0; }
    
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() { return "<native fn>"; }
        }, false, false, null);
        new Log(this);
        globals.define("JVM", new JavaClass().call(this, List.of()), false, false, null);
        globals.define("import", new BleepCallable() {
            @Override
            public int arity(List<Object> arguments) { return 1; }
    
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof String str) {
                    Interpreter interpreter1 = new Interpreter();
                    Environment env = Bleep.run(new File(str), interpreter1);
                    return new ExportDataClass(env).call(interpreter, List.of());
                }

                throw new RuntimeError(null, "Argument must be of type string.");
            }

            @Override
            public String toString() { return "<native fn>"; }
        }, false, false, null);
        globals.define("List", new BleepCallable() {
            @Override
            public boolean canHaveInfiniteArgs(List<Object> arguments) {
                return true;
            }

            @Override
            public int arity(List<Object> arguments) {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return new ArrayList<>(arguments);
            }

            @Override
            public String toString() { return "<native fn>"; }
        }, false, false, null);
        globals.define("String", String.class, false, false, null);
        globals.define("JVMObject", Object.class, false, false, null);
    }

    protected void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Bleep.runtimeError(error);
        }
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    protected void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    protected void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        Object superclass = null;
        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass);
            /*if (!(superclass instanceof BleepClass bleepClass)) {
                throw new RuntimeError(stmt.superclass.name, "Superclass must be a class.");
            }*/
            if (superclass instanceof Class<?> clazz) {
                try {
                    superclass = clazz.getConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeError(stmt.name, "Could not get instance of super class", e);
                }
            }
        }

        environment.define(stmt.name.lexeme, null);

        if (stmt.superclass != null) {
            environment = new Environment(environment);
            environment.define("super", superclass);
        }

        Map<String, BleepCallable> methods = new HashMap<>();
        for (Stmt.Function method : stmt.methods) {
            BleepFunction function = new BleepFunction(method, environment,
                                                   method.name.lexeme.equals("init"));
            methods.put(method.name.lexeme, function);
        }

        BleepClass klass = new BleepClass(stmt.name.lexeme, superclass, methods);

        if (superclass != null) {
            environment = environment.enclosing;
        }

        environment.assign(stmt.name, klass);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        BleepFunction function = new BleepFunction(stmt, environment, false);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);

        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value, true, true, stmt.name);
        return null;
    }

    @Override
    public Void visitConstStmt(Stmt.Const stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value, false, true, stmt.name);
        return null;
    }

    @Override
    public Void visitFieldStmt(Stmt.Field stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value, false, false, stmt.name);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Void visitRepeatStmt(Stmt.Repeat stmt) {
        Object count = evaluate(stmt.count);
        if (count == null) {
            execute(stmt.body);
        } else if (count instanceof Double d) {
            for (int i = 0; i < d; ++i) {
                execute(stmt.body);
            }
        } else {
            throw new RuntimeError(stmt.line, "Number required as input to repeat");
        }
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);

        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }

        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right); 

        switch (expr.operator.type) {
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case MINUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left - (double)right;
                }

                if (left instanceof String stringL && right instanceof String stringR) {
                    return stringL.replace(stringR, "");
                }

                throw new RuntimeError(expr.operator,
                        "Operands must be two numbers or two strings.");
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }

                return left.toString() + right.toString();
            case SLASH:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left / (double)right;
                }

                if (left instanceof String stringL && right instanceof String stringR) {
                    return stringL.split(stringR).length - 1;
                }

                throw new RuntimeError(expr.operator,
                        "Operands must be two numbers or two strings.");
            case STAR:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left * (double)right;
                }

                if (left instanceof String stringL && right instanceof Double doubleR) {
                    return stringL.repeat(doubleR.intValue());
                }

                if (left instanceof Double doubleL && right instanceof String stringR) {
                    return stringR.repeat(doubleL.intValue());
                }

                throw new RuntimeError(expr.operator,
                        "Operands must be two numbers or two strings.");
        }

        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) { 
            arguments.add(evaluate(argument));
        }

        if (callee instanceof BleepCallable function) {
            if (!function.canHaveInfiniteArgs(arguments)) {
                if (arguments.size() != function.arity(arguments)) {
                    throw new RuntimeError(expr.paren, "Expected " +
                            function.arity(arguments) + " arguments but got " +
                            arguments.size() + ".");
                }
            }

            return function.call(this, arguments);
        } else {
            if (callee instanceof Class<?> clazz) {
                List<Class<?>> clazzes = new ArrayList<>();
                arguments.forEach(obj -> clazzes.add(obj.getClass()));
                try {
                    return clazz.getConstructor(clazzes.toArray(new Class[]{})).newInstance(arguments.toArray());
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeError(expr.paren, "Unable to create new JVM class instance", e);
                }
            }
        }
        throw new RuntimeError(expr.paren, "Unable to execute call.");
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.object);
        if (object instanceof BleepInstance) {
            return ((BleepInstance) object).get(expr.name);
        }

        try {
            Class<?> clazz;
            boolean stat = false;
            if (object instanceof Class<?> c) {
                clazz = c;
                stat = true;
            } else {
                clazz = object.getClass();
            }
            if (expr.name.lexeme.equals("class")) {
                return clazz;
            }
            for (Field field : clazz.getFields()) {
                if (expr.name.lexeme.equals(field.getName())) {
                    if (stat) return field.get(null);
                    return field.get(object);
                }
            }

            for (Method method : clazz.getMethods()) {
                if (expr.name.lexeme.equals(method.getName())) {
                    boolean finalStat = stat;
                    return new BleepCallable() {
                        @Override
                        public boolean canHaveInfiniteArgs(List<Object> arguments) {
                            return true;
                        }

                        @Override
                        public int arity(List<Object> arguments) {
                            return 0;
                        }

                        @Override
                        public Object call(Interpreter interpreter, List<Object> arguments) {
                            try {
                                List<Class<?>> clazzes = new ArrayList<>();
                                arguments.forEach(obj -> clazzes.add(obj.getClass()));
                                return clazz.getMethod(expr.name.lexeme, clazzes.toArray(new Class[]{})).invoke(finalStat ? null : object, arguments.toArray());
                            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                                try {
                                    List<Class<?>> clazzes = new ArrayList<>();
                                    arguments.forEach(obj -> clazzes.add(Object.class));
                                    return clazz.getMethod(expr.name.lexeme, clazzes.toArray(new Class[]{})).invoke(finalStat ? null : object, arguments.toArray());
                                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                                    throw new RuntimeError(expr.name, "Unable to execute JVM method", ex);
                                }
                            }
                        }
                    };
                }
            }
        } catch (Exception e) {
            throw new RuntimeError(expr.name, "Unable to get JVM field or method", e);
        }

        throw new RuntimeError(expr.name, "Only instances or native JVM classes have properties. Perhaps that was not a public JVM field or method?");
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.object);

        if (!(object instanceof BleepInstance)) {
            throw new RuntimeError(expr.name, "Only instances have fields.");
        }

        Object value = evaluate(expr.value);
        ((BleepInstance)object).set(expr.name, value);
        return value;
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        int distance = locals.get(expr);
        Object superclass = environment.getAt(distance, "super");

        BleepInstance object = (BleepInstance)environment.getAt(distance - 1, "this");

        BleepCallable method;
        if (superclass instanceof BleepClass bleepClass) {
            method = bleepClass.findMethod(expr.method.lexeme);
        } else {
            method = new BleepCallable() {
                @Override
                public boolean canHaveInfiniteArgs(List<Object> arguments) {
                    return true;
                }

                @Override
                public int arity(List<Object> arguments) {
                    return 0;
                }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    List<Class<?>> clazzes = new ArrayList<>();
                    arguments.forEach(obj -> clazzes.add(obj.getClass()));
                    try {
                        return superclass.getClass().getMethod(expr.method.lexeme, clazzes.toArray(new Class[]{})).invoke(superclass, arguments.toArray());
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        throw new RuntimeError(expr.method, "Unable to call JVM method", e);
                    }
                }
            };
        }

        if (method == null) {
            throw new RuntimeError(expr.method, "Undefined property '" + expr.method.lexeme + "'.");
        }

        return method.bind(object, this);
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookUpVariable(expr.keyword, expr);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        return switch (expr.operator.type) {
            case BANG -> !isTruthy(right);
            case MINUS -> {
                checkNumberOperand(expr.operator, right);
                yield -(double) right;
            }
            default -> null;
        };

    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookUpVariable(expr.name, expr);
    }

    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
    
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
    
        return a.equals(b);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";
    
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
    
        return object.toString();
    }
}
