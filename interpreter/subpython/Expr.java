package interpreter.subpython;

import java.util.List;

abstract class Expr {
    static class Binary extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public String toString() {
            return left + " " + operator + " " + right;
        }
    }

    static class Grouping extends Expr {
        final Expr expression;

        Grouping(Expr expression) {
            this.expression = expression;
        }

    }

    static class Literal extends Expr {
        final Object value;

        Literal(Object value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }

    static class Unary extends Expr {
        final Token operator;
        final Expr right;

        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }
    }

    static class Assignment extends Expr {
        final Token name;
        final Expr value;

        Assignment(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }
    }

    static class Variable extends Expr {
        final Token name;

        Variable(Token name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name.lexeme;
        }
    }

    static class Logical extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
        
    }

    static class List_ extends Expr {
        final List<Expr> elements;

        List_(List<Expr> elements) {
            this.elements = elements;
        }
    }

    static class Index extends Expr {
        final Token identifier;
        final Expr start;
        final Expr end;
        final Expr step;

        Index(Token identifier, Expr start, Expr end, Expr step) {
            this.identifier = identifier;
            this.start = start;
            this.end = end;
            this.step = step;
        }
    }

    static class Call extends Expr {
        final Token identifier;
        final List<Expr> arguments;

        Call(Token identifier, List<Expr> arguments) {
            this.identifier = identifier;
            this.arguments = arguments;
        }
    }

}
