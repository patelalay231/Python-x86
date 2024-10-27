package src.interpreter.subpython;

class AstPrinter {
    
    String print(Expr expr) {
        return printExpr(expr);
    }

    private String printExpr(Expr expr) {
        switch (expr) {
            case Expr.Binary binary -> {
                return printBinaryExpr(binary);
            }
            case Expr.Grouping grouping -> {
                return printGroupingExpr(grouping);
            }
            case Expr.Literal literal -> {
                return printLiteralExpr(literal);
            }
            case Expr.Unary unary -> {
                return printUnaryExpr(unary);
            }
            default -> {
            }
        }
        return ""; // or throw an error if you prefer
    }

    private String printBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    private String printGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    private String printLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "NONE";
        return expr.value.toString();
    }

    private String printUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name);

        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(printExpr(expr));
        }

        builder.append(")");
        return builder.toString();
    }

    /* Test the AstPrinter */
    // public static void main(String[] args) {
    //     Expr expression = new Expr.Binary(
    //         new Expr.Unary(
    //             new Token(TokenType.MINUS, "-", null, 1),
    //             new Expr.Literal(123)
    //         ),
    //         new Token(TokenType.STAR, "*", null, 1),
    //         new Expr.Grouping(
    //             new Expr.Literal(45.67)
    //         )
    //     );
    //     System.out.println(new AstPrinter().print(expression));
    // }
}
