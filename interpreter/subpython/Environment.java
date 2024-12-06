package interpreter.subpython;

import java.util.HashMap;
import java.util.Map;

class Environment {

    private final Map<String, Object> values = new HashMap<>();
    final Environment enclosing;

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing){
        this.enclosing = enclosing;
    }


    void define(String name, Object value) {
        Environment scope = findScope(name);
        if (scope != null) {
            scope.values.put(name, value);  // Update in the existing scope
        } else {
            values.put(name, value);  // Define as new in the current scope
        }
    }

    private Environment findScope(String name) {
        if (values.containsKey(name)) {
            return this;
        }
        if (enclosing != null) {
            return enclosing.findScope(name);
        }
        return null;  // Variable is not defined in any enclosing scope
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        if(enclosing != null){
            return enclosing.get(name);
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
    
    @Override
    public String toString() {
        return values.toString();
    }
}