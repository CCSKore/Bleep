package net.kore.bleep;

import java.util.List;

public abstract class Expr {
    protected interface Visitor<R> {
        R visitAssignExpr(Assign expr);
        R visitBinaryExpr(Binary expr);
        R visitCallExpr(Call expr);
        R visitGetExpr(Get expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitLogicalExpr(Logical expr);
        R visitSetExpr(Set expr);
        R visitSuperExpr(Super expr);
        R visitThisExpr(This expr);
        R visitUnaryExpr(Unary expr);
        R visitVariableExpr(Variable expr);
    }

    protected abstract <R> R accept(Visitor<R> visitor);

    protected static class Assign extends Expr {
        protected Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }
    
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }

        protected final Token name;
        protected final Expr value;
    }

    protected static class Binary extends Expr {
        protected Binary(Expr left, Token operator, Expr right) {
            this.left = left;this.operator = operator;this.right = right;
        }

        protected final Expr left;final Token operator;final Expr right;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    protected static class Call extends Expr {
        protected Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }
    
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }

        protected final Expr callee;
        protected final Token paren;
        protected final List<Expr> arguments;
    }

    protected static class Get extends Expr {
        protected Get(Expr object, Token name) {
            this.object = object;
            this.name = name;
        }
    
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }

        protected final Expr object;
        protected final Token name;
    }

    protected static class Grouping extends Expr {
        protected Grouping(Expr expression) {this.expression = expression;}

        protected final Expr expression;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    protected static class Literal extends Expr {
        protected Literal(Object value) {this.value = value;}

        protected final Object value;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    protected static class Logical extends Expr {
        protected Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
    
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }

        protected final Expr left;
        protected final Token operator;
        protected final Expr right;
    }

    protected static class Set extends Expr {
        protected Set(Expr object, Token name, Expr value) {
            this.object = object;
            this.name = name;
            this.value = value;
        }
    
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }

        protected final Expr object;
        protected final Token name;
        protected final Expr value;
    }

    protected static class Super extends Expr {
        protected Super(Token keyword, Token method) {
            this.keyword = keyword;
            this.method = method;
        }
    
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSuperExpr(this);
        }

        protected final Token keyword;
        protected final Token method;
    }

    protected static class This extends Expr {
        protected This(Token keyword) {
            this.keyword = keyword;
        }
    
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitThisExpr(this);
        }
    
        protected final Token keyword;
    }

    protected static class Unary extends Expr {
        protected Unary(Token operator, Expr right) {
            this.operator = operator;this.right = right;
        }

        protected final Token operator;
        protected final Expr right;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    protected static class Variable extends Expr {
        protected Variable(Token name) {
            this.name = name;
        }
    
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
    
        final Token name;
    }
}
