package net.kore.bleep;

import java.util.List;

public abstract class Stmt {
    protected interface Visitor<R> {
        R visitBlockStmt(Block stmt);
        R visitClassStmt(Class stmt);
        R visitExpressionStmt(Expression stmt);
        R visitFunctionStmt(Function stmt);
        R visitIfStmt(If stmt);
        R visitReturnStmt(Return stmt);
        R visitVarStmt(Var stmt);
        R visitConstStmt(Const stmt);
        R visitFieldStmt(Field stmt);
        R visitWhileStmt(While stmt);
        R visitRepeatStmt(Repeat stmt);
    }

    protected static class Block extends Stmt {
        Block(List<Stmt> statements) {
            this.statements = statements;
        }
    
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    
        final List<Stmt> statements;
    }

    protected static class Class extends Stmt {
        Class(Token name, Expr.Variable superclass, List<Stmt.Function> methods) {
            this.name = name;
            this.superclass = superclass;
            this.methods = methods;
        }
    
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitClassStmt(this);
        }
    
        final Token name;
        final Expr.Variable superclass;
        final List<Stmt.Function> methods;
    }

    protected static class Expression extends Stmt {
        Expression(Expr expression) {
            this.expression = expression;
        }
    
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    
        final Expr expression;
    }

    protected static class Function extends Stmt {
        Function(Token name, List<Token> params, List<Stmt> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }
    
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStmt(this);
        }
    
        final Token name;
        final List<Token> params;
        final List<Stmt> body;
    }

    protected static class If extends Stmt {
        If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }
    
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    
        final Expr condition;
        final Stmt thenBranch;
        final Stmt elseBranch;
    }

    protected static class Return extends Stmt {
        Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }
    
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    
        final Token keyword;
        final Expr value;
    }

    protected static class Var extends Stmt {
        Var(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }
    
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }
    
        final Token name;
        final Expr initializer;
    }

    protected static class Const extends Stmt {
        Const(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitConstStmt(this);
        }

        final Token name;
        final Expr initializer;
    }

    protected static class Field extends Stmt {
        Field(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFieldStmt(this);
        }

        final Token name;
        final Expr initializer;
    }

    protected static class While extends Stmt {
        While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }
    
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    
        final Expr condition;
        final Stmt body;
    }

    protected static class Repeat extends Stmt {
        Repeat(Expr count, Stmt body, int line) {
            this.count = count;
            this.body = body;
            this.line = line;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitRepeatStmt(this);
        }

        final Expr count;
        final Stmt body;
        final int line;
    }

    abstract <R> R accept(Visitor<R> visitor);
}
