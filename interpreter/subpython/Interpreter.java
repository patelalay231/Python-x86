package interpreter.subpython;

import java.util.List;


class Interpreter extends RuntimeException { 

    private Environment environment = new Environment();

    public void interpreter(List<Stmt> statments){
        try{
            for(Stmt statement : statments){
                evaluate(statement);
            }
        } catch (RuntimeError error){
            Subpython.error(error.token, error.getMessage());
        }
    }
    
    private String stringify(Object object) {
        if (object == null) return "None";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    public void evaluate(Stmt stmt) {
        if(stmt instanceof Stmt.Expression expression){
            evaluateExprStmt(expression.expression);
        }
        else if(stmt instanceof Stmt.Print print){
            evaluatePrintStmt(print.expression);
        }
        else if(stmt instanceof Stmt.Assignment assignment){
            evaluateAssignStmt(assignment);
        }
        else if(stmt instanceof Stmt.Block block){
            evaluateBlockStmt(block,new Environment(environment));
        }
    }

    private void evaluatePrintStmt(Expr expression) {
        Object value = evaluateExprStmt(expression);
        System.out.println(stringify(value));
    }

    private void evaluateAssignStmt(Stmt.Assignment assignment) {
        try {
            Object value = assignment.initializer;
            if(value != null){
                value = evaluateExprStmt(assignment.initializer);
            }
            environment.define(assignment.name.lexeme, value);
        } catch (RuntimeError error) {
            throw new RuntimeError(assignment.name, error.getMessage());
        }
    }

    private void evaluateBlockStmt(Stmt.Block block, Environment environment) {
        Environment previous = this.environment;

        try {
            this.environment = environment;
            for (Stmt statement : block.statements) {
                evaluate(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    private Object evaluateAssignStmt(Expr.Assignment expr) {
        Object value = evaluateExprStmt(expr.value); // Recursively evaluate RHS
        environment.define(expr.name.lexeme, value); // Store the evaluated value in environment
        return value;
    }

    private Object evaluateExprStmt(Expr expr) {
        switch (expr) {
            case Expr.Binary binary -> {
                return evaluateBinaryExpr(binary);
            }
            case Expr.Grouping grouping -> {
                return evaluateGroupingExpr(grouping);
            }
            case Expr.Literal literal -> {
                return evaluateLiteralExpr(literal);
            }
            case Expr.Unary unary -> {
                return evaluateUnaryExpr(unary);
            }
            case Expr.Variable variable -> {
                return environment.get(variable.name);
            }
            case Expr.Assignment assignment -> {
                return evaluateAssignStmt(assignment);
            }
            default -> {
                
            }
        }
        return null;
    }

    // Truthy
    private boolean isTruthy(Object right) {
        if (right == null) return false;
        if (right instanceof Boolean) return (boolean) right;
        return true;
    }
    
    // Errors
    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    // Evaluators
    public Object evaluateLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    public Object evaluateGroupingExpr(Expr.Grouping expr) {
        return evaluateExprStmt(expr.expression);
    }

    public Object evaluateUnaryExpr(Expr.Unary expr) {
        Object right = evaluateExprStmt(expr.right);

        switch (expr.operator.type) {
            case MINUS -> {
                checkNumberOperand(expr.operator, right);
                return -(double) right;
            }
            case BANG -> {
                return !isTruthy(right);
            }
        }
        return null; // or throw an error if you prefer
    }

    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null) return true;
        if (left == null) return false;
        return left.equals(right);
    }

    public Object evaluateBinaryExpr(Expr.Binary expr){
        Object left = evaluateExprStmt(expr.left);
        Object right = evaluateExprStmt(expr.right);

        switch (expr.operator.type){
            case MINUS -> {
                checkNumberOperands(expr.operator, left,right);
                return (double) left - (double) right;
            }
            case PLUS -> {
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                if (left instanceof String && right instanceof Double) {
                    return (String) left + stringify(right);
                }
                if (left instanceof Double && right instanceof String) {
                    return stringify(left) + (String) right;
                }
                throw new RuntimeError(expr.operator,"Operands must be two numbers or two strings.");
            }
            case SLASH -> {
                checkNumberOperands(expr.operator, left,right);
                if((double) right == 0){
                    throw new RuntimeError(expr.operator, "Division by zero.");
                }
                return (double) left / (double) right;
            }
            case STAR -> {
                checkNumberOperands(expr.operator, left,right);
                return (double) left * (double) right;
            }
            case GREATER -> {
                checkNumberOperands(expr.operator, left,right);
                return (double) left > (double) right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left,right);
                return (double) left >= (double) right;
            }
            case LESS -> {
                checkNumberOperands(expr.operator, left,right);
                return (double) left < (double) right;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left,right);
                return (double) left <= (double) right;
            }
            case BANG_EQUAL -> {
                return !isEqual(left, right);
            }
            case EQUAL_EQUAL -> {
                return isEqual(left, right);
            }
            case POW -> {
                checkNumberOperands(expr.operator, left,right);
                return Math.pow((double) left, (double) right);
            }
            case MOD -> {
                checkNumberOperands(expr.operator, left,right);
                return (double) left % (double) right;
            }
        }
        return null; // or throw an error if you prefer
    }
    

}