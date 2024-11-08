package interpreter.subpython;

import java.util.List;

class Function {
    final Token name;
    final List<Token> params;
    final List<Stmt> body;

    Function(Stmt.Function function) {
        this.name = function.name;
        this.params = function.params;
        this.body = function.body;
    }    
}