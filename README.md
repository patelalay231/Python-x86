# Python-x86 Compiler

A Java-based Python to x86 assembly compiler, following the implementation approach from "Crafting Interpreters". Currently in active development with new features being added regularly.

## Current Version Features

### Implemented
- arithmetic expression evaluation
- Variable, Local scoping
## Project Structure

```
Python-x86/
├── interpreter/
│   ├── Documents/
│   │   ├── Grammar/         # Parser grammar documentation
│   │   └── command_to_run/  # Execution commands
│   └── src/                 # Source code
├── tests/
│   └── arithmetic/          # Test cases
└── README.md
```

## 🏃‍♂️ Getting Started

### Prerequisites
- Java Development Kit (JDK) 8+
- Java compiler in PATH

### Quick Start
```bash
# Clone repository
git clone https://github.com/patelalay231/Python-x86.git
cd Python-x86


## 📖 Current Grammar

```ebnf
expression → equality ;
equality   → comparison ( ( "!=" | "==" ) comparison )* ;
comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term       → factor ( ( "-" | "+" ) factor )* ;
factor     → unary ( ( "/" | "*" ) unary )* ;
unary      → ( "!" | "-" ) unary | power ;
pow      → primary ( "^" pow )* ;
primary    → NUMBER | STRING | "true" | "false" | "none" | "(" expression ")" ;

```



## 👥 Contact

Your Name - alay.patel@iitgn.ac.in
Project Link: [https://github.com/patelalay231/Python-x86.git](https://github.com/patelalay231/Python-x86.git)
