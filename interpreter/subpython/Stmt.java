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

}
