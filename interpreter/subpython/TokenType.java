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
    COLON,         // ':'  - Starting a block
    INDENT,        // '->' - Indicates the start of an indented block
    DEDENT,        // '<-' - Indicates the end of an indented block

    // One or two character tokens.
    BANG,          // '!'   - Logical negation (alternative syntax)
    BANG_EQUAL,    // '!='  - Not equal comparison
    EQUAL,         // '='   - Assignment operator
    EQUAL_EQUAL,   // '=='  - Equality comparison
    GREATER,       // '>'   - Greater-than comparison
    GREATER_EQUAL, // '>='  - Greater-than-or-equal comparison
    LESS,          // '<'   - Less-than comparison
    LESS_EQUAL,    // '<='  - Less-than-or-equal comparison

    // Bitwise operators.
    BITWISE_AND,   // '&'  - Bitwise AND
    BITWISE_OR,    // '|'  - Bitwise OR
    BITWISE_XOR,   // '^'  - Bitwise XOR
    BITWISE_NOT,   // '~'  - Bitwise NOT
    LEFT_SHIFT,    // '<<' - Left bitwise shift
    RIGHT_SHIFT,   // '>>' - Right bitwise shift

    // Logical operators.
    AND,           // 'and' - Logical conjunction
    OR,            // 'or' - Logical disjunction
    NOT,           // 'not' - Logical negation

    // Literals.
    IDENTIFIER,    // Names for variables, functions, etc.
    STRING,        // String literals enclosed in quotes
    NUMBER,        // Numeric literals (integers or floats)

    // Keywords.
    ELIF,          // 'elif' - Part of conditional branches
    ELSE,          // 'else' - Part of conditional branches
    IF,            // 'if' - Start of a conditional
    NONE,          // 'None' - Represents the absence of a value
    WHILE,         // 'while' - Starts a loop with a condition
    FOR,           // 'for' - Starts a loop iterating over items
    RETURN,        // 'return' - Exits a function and returns a value
    PRINT,         // 'print' - Outputs a value
    TRUE,          // 'True' - Boolean literal for true
    FALSE,         // 'False' - Boolean literal for false

    // End of file token.
    EOF            // Signifies the end of the input
}
