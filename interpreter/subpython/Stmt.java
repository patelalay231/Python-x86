package interpreter.subpython;

import java.util.List;

abstract class Stmt {
    static class Expression extends Stmt {
        final Expr expression;

        Expression(Expr expression) {
            this.expression = expression;
        }
    }

    static class If extends Stmt {
        final List<Expr> condition;
        final List<Stmt> thenBranch;
        final Stmt elseBranch;

        If(List<Expr> condition, List<Stmt> thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }
    }

    static class Print extends Stmt {
        final Expr expression;

        Print(Expr expression) {
            this.expression = expression;
        }
    }

    static class Assignment extends Stmt {
        final Token name;
        final Expr initializer;

        Assignment(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }
    }

    static class Block extends Stmt {
        final List<Stmt> statements;

        Block(List<Stmt> statements) {
            this.statements = statements;
        }
    }

    static class While extends Stmt {
        final Expr condition;
        final Stmt body;

        While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }
    }

    static class For extends Stmt {
        final Token name;
        final Expr start;
        final Expr end;
        final Expr step;
        final Stmt body;

        For(Token name, Expr start, Expr end, Expr step, Stmt body) {
            this.name = name;
            this.start = start;
            this.end = end;
            this.step = step;
            this.body = body;
        }
    }

    static class ForIterable extends Stmt {
        final Token name;
        final Expr iterable;
        final Stmt body;

        ForIterable(Token name, Expr iterable, Stmt body) {
            this.name = name;
            this.iterable = iterable;
            this.body = body;
        }
    }

}
