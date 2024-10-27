package interpreter.subpython;


class Interpreter extends RuntimeException { 

    public String interpreter(Expr expression){
        try{
            Object value = evaluate(expression);
            return stringify(value);
        } catch (RuntimeError error){
            Subpython.error(error.token, error.getMessage());
        }
        return null;
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

    public Object evaluate(Expr expr) {
        return evaluateExpr(expr);
    }

    private Object evaluateExpr(Expr expr) {
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
        return evaluateExpr(expr.expression);
    }

    public Object evaluateUnaryExpr(Expr.Unary expr) {
        Object right = evaluateExpr(expr.right);

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
        Object left = evaluateExpr(expr.left);
        Object right = evaluateExpr(expr.right);

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