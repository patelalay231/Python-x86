package interpreter.subpython;

import static interpreter.subpython.TokenType.*;
import java.util.ArrayList;
import java.util.List;

class Parser {
    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Subpython.error(token, message);
        return new ParseError();
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == NEW_LINE) return;
            switch (peek().type) {
                case IF, WHILE, FOR, RETURN, PRINT, IDENTIFIER -> { return; }
            }
            advance();
        }
    }

    // Program → stmt* EOF ;
    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(statement());
        }
        return statements;
    }

    // statement → assignmentStmt | exprStmt | printStmt | blockStmt;
    private Stmt statement() {
        if (match(NEW_LINE)) {
            while (match(NEW_LINE)) {}
        }
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(IDENTIFIER) && check(EQUAL)) return assignmentStatement();
        return expressionStatement();
    }

    // ifStmt -> IF expression COLON NEW_LINE blockStmt (ELIF expression COLON NEW_LINE blockStmt)* ( ELSE COLON NEW_LINE blockStmt )? ;
    private Stmt ifStatement() {
        List<Expr> condition = new ArrayList<>();
        List<Stmt> thenBranch = new ArrayList<>();
        condition.add(expression()); // Parse the condition expression
        consume(COLON, "Expect ':' after 'if' condition.");
        consume(NEW_LINE, "Expect newline after ':' in if statement.");
        thenBranch.add(new Stmt.Block(blockStmt()));

        while(match(ELIF)){
            condition.add(expression());
            consume(COLON, "Expect ':' after 'if' condition.");
            consume(NEW_LINE, "Expect newline after ':' in if statement.");
            if (match(NEW_LINE)) {
                while (match(NEW_LINE)) {}
            }

            thenBranch.add(new Stmt.Block(blockStmt()));
            if (match(NEW_LINE)) {
                while (match(NEW_LINE)) {}
            }

        }

        // we can storr the List<Stmt> as Stmt type because Stmt.Block is a subclass of Stmt
        Stmt elseBranch = null;
        if (match(ELSE)) {
            consume(COLON, "Expect ':' after 'else'.");
            consume(NEW_LINE, "Expect newline after ':' in else statement.");
            if (match(NEW_LINE)) {
                while (match(NEW_LINE)) {}
            }

            elseBranch = new Stmt.Block(blockStmt());
            if (match(NEW_LINE)) {
                while (match(NEW_LINE)) {}
            }

        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    // blockStmt → INDENT statement+ DEDENT ;
    private List<Stmt> blockStmt() {
        List<Stmt> statements = new ArrayList<>();

        consume(INDENT, "Expect indentation to start block.");
        
        while (!check(DEDENT) && !isAtEnd()) {
            statements.add(statement());
        }
        
        consume(DEDENT, "Expect indentation to end block.");
        return statements;
    }



    // assignmentStmt → IDENTIFIER "=" expression NEW_LINE* | logic_or;
    private Stmt assignmentStatement() {
        Token name = previous();
        consume(EQUAL, "Expect '=' after variable name.");
        Expr value = expression();

        
        if (match(NEW_LINE)) {
            while (match(NEW_LINE)) {}
        }

        return new Stmt.Assignment(name, value);
    }

    // printStmt → PRINT LEFT_PAREN expression RIGHT_PAREN NEW_LINE* ;
    private Stmt printStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'print'.");
        Expr value = expression();
        consume(RIGHT_PAREN, "Expect ')' after value.");
        
        if (match(NEW_LINE)) {
            while (match(NEW_LINE)) {}
        }
        return new Stmt.Print(value);
    }

    // exprStmt → expression NEW_LINE* ;
    private Stmt expressionStatement() {
        Expr expr = expression();

        if (match(NEW_LINE)) {
            while (match(NEW_LINE)) {}
        }

        return new Stmt.Expression(expr);
    }

    // expression → assignment ;
    private Expr expression() {
        return assignment();
    }

    // assignment → IDENTIFIER "=" assignment | logic_or ;
    private Expr assignment() {
        Expr expr = logicOr();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();  // Right-associative assignment

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assignment(name, value);
            }

            throw error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr logicOr(){
        Expr expr = logicAnd();
        while(match(OR)){
            Token operator = previous();
            Expr right = logicAnd();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr logicAnd(){
        Expr expr = equality();
        while(match(AND)){
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    // equality → comparison ( ( "!=" | "==" ) comparison )* ;
    private Expr equality() {
        Expr expr = comparison();
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    private Expr comparison() {
        Expr expr = term();
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // term → factor ( ( "-" | "+" ) factor )* ;
    private Expr term() {
        Expr expr = factor();
        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // factor → unary ( ( "/" | "*" | "%" ) unary )* ;
    private Expr factor() {
        Expr expr = unary();
        while (match(SLASH, STAR, MOD)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // unary → ( "!" | "-" ) unary | pow ;
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();

            // Special handling for -0 to 0
            if (operator.type == MINUS && right instanceof Expr.Literal) {
                Object value = ((Expr.Literal) right).value;
                if (value instanceof Double && (Double) value == 0.0) {
                    return new Expr.Literal(0.0);
                }
            }
            return new Expr.Unary(operator, right);
        }
        return pow();
    }

    // pow → primary ( "^" pow )* ;
    private Expr pow() {
        Expr expr = primary();
        while (match(POW)) {
            Token operator = previous();
            Expr right = pow();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // primary → NUMBER | STRING | "false" | "true" | "None" | "(" expression ")" ;
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NONE)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        throw error(peek(), "Expect expression.");
    }
}
