package interpreter.subpython;

import static interpreter.subpython.TokenType.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class Scanner{
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        
        // Keywords mapped to their respective token types.
        keywords.put("and", AND);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("True", TRUE);
        keywords.put("else", ELSE);
        keywords.put("else", ELSE);
        keywords.put("False", FALSE);
        keywords.put("for", FOR);
        keywords.put("if", IF);
        keywords.put("None", NONE); // Adjusted from 'nil' to 'none'
    }

    Scanner(String source){
        this.source = source;
    }

    List<Token> scanTokens(){
        while(!isAtEnd()){
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }
    
    private boolean isAtEnd() {
        return current >= source.length();
    }

    // it's consume the char and return the char
    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    // it's consume the char and return the char if it's match with the expected char
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    // it's not conssume the char, just return the current char
    private char peek(){
        if(isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    } 

     private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        if(type == NEW_LINE) text = "\\n";
        tokens.add(new Token(type, text, literal, line));   
    }

    private void scanToken() {
        char c = advance();
        switch(c){
            case '(' -> addToken(LEFT_PAREN);
            case ')' -> addToken(RIGHT_PAREN);
            case '{' -> addToken(LEFT_BRACE);
            case '}' -> addToken(RIGHT_BRACE);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(DOT);
            case '-' -> addToken(MINUS);
            case '+' -> addToken(PLUS);
            case '*' -> {
                if(match('*')) addToken(POW);
                else addToken(STAR);
            }
            case '%' -> addToken(MOD);
            case '!' -> addToken(match('=') ? BANG_EQUAL : BANG);
            case '=' -> addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);
            case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);
            case '/' -> addToken(match('=') ? SLASH : SLASH);
            case '#' -> {
                while(peek() != '\n' && !isAtEnd()) advance();
            }
            case ' ', '\r', '\t' -> {
            }
            case '\n' -> {
                addToken(NEW_LINE);
                line++;
            }
            case '"' -> string();
            default -> {
                if(isDigit(c)){
                    number();
                }
                else if (isAlpha(c)) {
                    identifier();
                }
                else{
                    Subpython.error(line, "Unexpected character.");
                }
            }
        }
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    } 

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void identifier(){
        while(isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if ("print".equals(text)) type = PRINT;
        if(type == null) type = IDENTIFIER;
        addToken(type);
    }
    
    private void number(){
        while(isDigit(peek())) advance();

        if(peek() == '.' && isDigit(peekNext())){
            // consume the "."
            advance();
            while(isDigit(peek())) advance();
        }
        
        addToken(NUMBER, Double.valueOf(source.substring(start, current)));
    }

    private void string(){
        while(peek() != '"' && !isAtEnd()){
            if(peek() == '\n') line++;
            advance();
        }

        if(isAtEnd()){
            Subpython.error(line, "Unterminated string.");
            return;
        }

        advance();

        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }
}
