package interpreter.subpython;

enum TokenType {
    // Single-character tokens.
    LEFT_PAREN,    // '('  - Opens a grouping or function call
    RIGHT_PAREN,   // ')'  - Closes a grouping or function call
    LEFT_BRACE,    // '{'  - Opens a block or scope
    RIGHT_BRACE,   // '}'  - Closes a block or scope
    COMMA,         // ','  - Separator in argument lists
    DOT,           // '.'  - Used for property access on objects
    MINUS,         // '-'  - Subtraction or negation operator
    PLUS,          // '+'  - Addition operator
    NEW_LINE,      // '\n' - Marks the end of a line
    SLASH,         // '/'  - Division operator
    STAR,          // '*'  - Multiplication operator
    MOD,           // '%'  - Modulus operator
    POW,           // '**' - Exponentiation operator

    // One or two character tokens.
    BANG,          // '!'   - Logical negation
    BANG_EQUAL,    // '!='  - Not equal comparison
    EQUAL,         // '='   - Assignment operator
    EQUAL_EQUAL,   // '=='  - Equality comparison
    GREATER,       // '>'   - Greater-than comparison
    GREATER_EQUAL, // '>='  - Greater-than-or-equal comparison
    LESS,          // '<'   - Less-than comparison
    LESS_EQUAL,    // '<='  - Less-than-or-equal comparison

    // Literals.
    IDENTIFIER,    // Names for variables, functions, etc.
    STRING,        // String literals enclosed in quotes
    NUMBER,        // Numeric literals (integers or floats)

    // Keywords.
    AND,           // 'and' - Logical conjunction
    ELSE,          // 'else' - Part of conditional branches
    IF,            // 'if' - Start of a conditional
    NONE,          // 'None' - Represents the absence of a value
    WHILE,         // 'while' - Starts a loop with a condition
    FOR,           // 'for' - Starts a loop iterating over items
    RETURN,        // 'return' - Exits a function and returns a value
    PRINT,         // 'print' - Outputs a value
    OR,            // 'or' - Logical disjunction
    TRUE,          // 'True' - Boolean literal for true
    FALSE,         // 'False' - Boolean literal for false

    // End of file token.
    EOF            // Signifies the end of the input
}
