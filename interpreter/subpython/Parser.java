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

    private boolean checkNext(TokenType type) {
        if (current + 1 >= tokens.size()) return false;
        return tokens.get(current + 1).type == type;
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

    // statement → assignmentStmt | exprStmt | printStmt | whileStmt | ifStmt | functionStmt;
    private Stmt statement() {
        if (match(NEW_LINE)) {
            while (match(NEW_LINE)) {}
        }
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (checkNext(EQUAL) && match(IDENTIFIER)) return assignmentStatement();
        if (match(WHILE)) return whileStatement();
        if (match(FOR)) return forStatement();
        if (match(DEF)) return functionStatement();
        if (match(RETURN)) return returnStatement();
        return expressionStatement();
    }

    // returnStmt → RETURN expression NEW_LINE* ;
    private Stmt returnStatement() {
        Expr value = expression();
        if (match(NEW_LINE)) {
            while (match(NEW_LINE)) {}
        }
        return new Stmt.Return(value);
    }

    // functionStmt → DEF IDENTIFIER LEFT_PAREN parameters? RIGHT_PAREN COLON NEW_LINE blockStmt ;
    private Stmt functionStatement() {
        Token name = consume(IDENTIFIER, "Expect function name.");
        consume(LEFT_PAREN, "Expect '(' after function name.");
        List<Token> parameters = new ArrayList<>();
        // Parsing parameters
        if (!check(RIGHT_PAREN)) {
            do {
                parameters.add(consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");
        consume(COLON, "Expect ':' after function declaration.");
        consume(NEW_LINE, "Expect newline after ':' in function declaration.");
        List<Stmt> body = blockStmt();
        return new Stmt.Function(name, parameters, body);
    }

    // whileStmt → WHILE expression COLON NEW_LINE blockStmt ;
    private Stmt whileStatement() {
        Expr condition = expression();
        consume(COLON, "Expect ':' after 'while' condition.");
        consume(NEW_LINE, "Expect newline after ':' in while statement.");
        Stmt body = new Stmt.Block(blockStmt());
        return new Stmt.While(condition, body);
    }

    // forStmt -> FOR IDENTIFIER IN (expression | RANGE LEFT_PAREN expression (COMMA expression (COMMA expression)?)? RIGHT_PAREN ) COLON blockStmt;
    private Stmt forStatement(){
        Token name = consume(IDENTIFIER, "Expect variable name after 'for'.");
        consume(IN, "Expect 'in' after variable name.");
        if(match(RANGE)){
            consume(LEFT_PAREN, "Expect '(' after 'range'.");
            Expr start = expression();
            Expr end = null;
            Expr step = null;
            if(match(COMMA)){
                end = expression();
                if(match(COMMA)){
                    step = expression();
                }
            }
            consume(RIGHT_PAREN, "Expect ')' after range arguments.");
            consume(COLON, "Expect ':' after 'for' statement.");
            consume(NEW_LINE, "Expect newline after ':' in for statement.");
            if(match(NEW_LINE)) {
                while (match(NEW_LINE)) {}
            }
            Stmt body = new Stmt.Block(blockStmt());
            return new Stmt.For(name, start, end, step, body);
        }
        else{
            Expr iterable = expression();
            consume(COLON, "Expect ':' after 'for' statement.");
            consume(NEW_LINE, "Expect newline after ':' in for statement.");
            if(match(NEW_LINE)) {
                while (match(NEW_LINE)) {}
            }
            Stmt body = new Stmt.Block(blockStmt());
            return new Stmt.ForIterable(name, iterable, body);
        }
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

    // expression → assignment | list;
    private Expr expression() {
        if(match(LEFT_BRACKET)) return list();
        return assignment();
    }

    // list -> LEFT_BRACKET ( expression (COMMA expression)* )? RIGHT_BRACKET;
    private Expr list(){
        List<Expr> elements = new ArrayList<>();
        if(!check(RIGHT_BRACKET)){
            do{
                elements.add(expression());
            } while(match(COMMA));
        }
        consume(RIGHT_BRACKET, "Expect ']' after list elements.");
        return new Expr.List_(elements);
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

    // primary -> NUMBER | STRING | "true" | "false" | "nil" | IDENTIFIER ( LEFT_BRACKET expression RIGHT_BRACKET | LEFT_PAREN arguments? RIGHT_PAREN)?; | LEFT_PAREN expression RIGHT_PAREN;
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
            // store identifier name
            Token identifier = previous();
            if (match(LEFT_BRACKET)) {
                Expr start = expression();
                Expr end = null;
                Expr step = null;
                if(match(COLON)){
                    end = expression();
                    if(match(COLON)){
                        step = expression();
                    }
                }
                consume(RIGHT_BRACKET, "Expect ']' after list index.");
                return new Expr.Index(identifier, start, end, step);
            }
            if (match(LEFT_PAREN)) {
                List<Expr> arguments = new ArrayList<>();
                if (!check(RIGHT_PAREN)) {
                    do {
                        arguments.add(expression());
                    } while (match(COMMA));
                }
                consume(RIGHT_PAREN, "Expect ')' after arguments.");
                return new Expr.Call(identifier, arguments);
            }
            return new Expr.Variable(identifier);
        }

        throw error(peek(), "Expect expression.");
    }
}
