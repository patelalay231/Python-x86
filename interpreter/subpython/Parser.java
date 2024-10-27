package interpreter.subpython;

import static interpreter.subpython.TokenType.*;
import java.util.List;

class Parser{
    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens){
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
                case IF:
                case WHILE:
                case FOR:
                case RETURN:
                case PRINT:
                case IDENTIFIER:
                    return;
            }

            advance();
        }
    }

    Expr parse(){
        try{
            return expression();
        } catch(ParseError error){
            return null;
        }
    }
    // expression → equality ;
    private Expr expression() {
        return equality();
    } 

    // equality → comparison ( ( "!=" | "==" ) comparison )* ;
    private Expr equality() {
        Expr expr = comparison();

        while(match(BANG_EQUAL, EQUAL_EQUAL)){
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;

    private Expr comparison() {
        Expr expr = term();

        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)){
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // term → factor ( ( "-" | "+" ) factor )* ;    
    private Expr term(){
        Expr expr = factor();

        while(match(MINUS, PLUS)){
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // factor → unary ( ( "/" | "*" | "%") unary )* ;
    private Expr factor(){
        Expr expr = unary();

        while(match(SLASH, STAR,MOD)){
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // unary → ( "!" | "-" ) unary | primary ;
    private Expr unary(){
        if(match(BANG, MINUS)){
            Token operator = previous();
            // write example of right 
            Expr right = unary();
            // handling -0 to 0
            if (operator.type == MINUS && right instanceof Expr.Literal) {
                Object value = ((Expr.Literal)right).value;
                if (value instanceof Double && (Double) value == 0.0) {
                    return new Expr.Literal(0.0);
                }
            }
            return new Expr.Unary(operator, right);
        }

        return pow();
    }

    // pow → primary ( "^" pow )* ;
    private Expr pow(){
        Expr expr = primary();

        while(match(POW)){
            Token operator = previous();
            Expr right = pow();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // primary → NUMBER | STRING | "false" | "true" | "None" | "(" expression ")" ;

    private Expr primary(){
        if(match(FALSE)) return new Expr.Literal(false);
        if(match(TRUE)) return new Expr.Literal(true);
        if(match(NONE)) return new Expr.Literal(null);

        if(match(NUMBER, STRING)){
            return new Expr.Literal(previous().literal);
        }

        if(match(LEFT_PAREN)){
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }

}