package interpreter.subpython;

import static interpreter.subpython.TokenType.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private Stack<Integer> indentationStack = new Stack<>();
    private boolean isBeginningOfLine = true;

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
        keywords.put("False", FALSE);
        keywords.put("for", FOR);
        keywords.put("if", IF);
        keywords.put("None", NONE);
        keywords.put("elif", ELIF);
        keywords.put("while", WHILE);
        keywords.put("in", IN);
        keywords.put("range", RANGE);
        keywords.put("def", DEF);
    }

    Scanner(String source) {
        this.source = source;
        indentationStack.push(0);
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            if (isBeginningOfLine) {
                handleIndentation();
            }
            scanToken();
        }

        // Close any remaining indentation levels
        while (indentationStack.size() > 1) {
            addToken(DEDENT);
            indentationStack.pop();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void handleIndentation() {
        start = current;
        int indent = 0;

        while (peek() == ' ' || peek() == '\t') {
            if (peek() == ' ') {
                indent++;
            } else if (peek() == '\t') {
                indent += 8; 
            }
            advance();
        }

        if (peek() == '\n' || peek() == '#') {
            isBeginningOfLine = true;
            return;
        }

        int previousIndent = indentationStack.peek();

        if (indent > previousIndent) {
            while (indent > previousIndent) {
                indentationStack.push(previousIndent + 4);
                addToken(INDENT);
                previousIndent += 4;
            }
        } else {
            while (indent < previousIndent) {
                indentationStack.pop();
                addToken(DEDENT);
                previousIndent = indentationStack.peek();
            }
        }
        start = current;
        isBeginningOfLine = false;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        if(TokenType.INDENT == type || TokenType.DEDENT == type || TokenType.NEW_LINE == type) {
            tokens.add(new Token(type, "", literal, line));
            return;
        }
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' -> addToken(LEFT_PAREN);
            case ')' -> addToken(RIGHT_PAREN);
            case '{' -> addToken(LEFT_BRACE);
            case '}' -> addToken(RIGHT_BRACE);
            case '[' -> addToken(LEFT_BRACKET);
            case ']' -> addToken(RIGHT_BRACKET);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(DOT);
            case '-' -> addToken(MINUS);
            case '+' -> addToken(PLUS);
            case ':' -> addToken(COLON);
            case '*' -> addToken(match('*') ? POW : STAR);
            case '%' -> addToken(MOD);
            case '!' -> addToken(match('=') ? BANG_EQUAL : BANG);
            case '=' -> addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);
            case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);
            case '/' -> addToken(SLASH);
            case '#' -> {
                while (peek() != '\n' && !isAtEnd()) advance();
            }
            case ' ', '\r', '\t' -> {
            }
            case '\n' -> {
                addToken(NEW_LINE);
                line++;
                isBeginningOfLine = true;
            }
            case '"' -> string();
            default -> {
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
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

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.valueOf(source.substring(start, current)));
    }

    private char peekNext() {
        return (current + 1 >= source.length()) ? '\0' : source.charAt(current + 1);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Subpython.error(line, "Unterminated string.");
            return;
        }

        advance();
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }
}
