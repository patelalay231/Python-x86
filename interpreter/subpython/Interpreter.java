package interpreter.subpython;

import java.util.ArrayList;
import java.util.List;


class Interpreter extends RuntimeException { 

    final Environment global = new Environment();
    private Environment environment = global;

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
        if (object instanceof List<?> listt) {
            StringBuilder builder = new StringBuilder("[");
            List<?> list = listt;
            for (int i = 0; i < list.size(); i++) {
                builder.append(stringify(list.get(i)));
                if (i != list.size() - 1) {
                    builder.append(", ");
                }
            }
            builder.append("]");
            return builder.toString();
        }
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
        switch (stmt) {
            case Stmt.Expression expression -> evaluateExprStmt(expression.expression);
            case Stmt.Print print -> evaluatePrintStmt(print.expression);
            case Stmt.Assignment assignment -> evaluateAssignStmt(assignment);
            case Stmt.Block block -> evaluateBlockStmt(block,new Environment(environment));
            case Stmt.If ifStmt -> evaluateIfStmt(ifStmt);
            case Stmt.While whileStmt -> evaluateWhileStmt(whileStmt);
            case Stmt.For forStmt -> evaluateForStmt(forStmt);
            case Stmt.ForIterable forIterable -> evaluateForIterable(forIterable);
            case Stmt.Function function -> evaluateFunctionStmt(function);
            case Stmt.Return returnStmt -> evaluateReturnStmt(returnStmt);
            default -> {
            }
        }
    }

    private void evaluateReturnStmt(Stmt.Return returnStmt) {
        Object value = null;
        if(returnStmt.value != null){
            value = evaluateExprStmt(returnStmt.value);
        }
        throw new Return(value);
    }

    private void evaluateFunctionStmt(Stmt.Function stmt) {
        Function function = new Function(stmt);
        environment.define(function.name.lexeme, function);
    }

    private void evaluateForIterable(Stmt.ForIterable forIterableStmt){
        Object iterable = evaluateExprStmt(forIterableStmt.iterable);
        String name = forIterableStmt.name.lexeme;

        if(!(iterable instanceof List<?> || iterable instanceof String)){
            throw new RuntimeError(forIterableStmt.name, "Only lists and strings can be iterated over.");
        }

        switch (iterable) {
            case List<?> list -> {
                for (Object element : list){
                    environment.define(name, element);
                    evaluate(forIterableStmt.body);
                }
            }
            case String string -> {
                for (int i = 0; i < string.length(); i++){
                    environment.define(name, string.charAt(i));
                    evaluate(forIterableStmt.body);
                }
            }
            default -> {
            }
        }
    }

    private void evaluateForStmt(Stmt.For forStmt){
        Object start = forStmt.start;
        Object end = forStmt.end;
        Object step = forStmt.step;
        if(start != null && end != null){
            start = evaluateExprStmt(forStmt.start);
            end = evaluateExprStmt(forStmt.end);
        }
        if(end == null){
            end = evaluateExprStmt(forStmt.start);
            start = 0.0;
        }
        if(step == null){
            step = 1.0;
        }
        else{
            step = evaluateExprStmt(forStmt.step);
        }
        
        String name = forStmt.name.lexeme;

        for (double i = (double) start; i < (double) end; i += (double) step){
            environment.define(name, i);
            evaluate(forStmt.body);
        }
    }

    private void evaluateWhileStmt(Stmt.While whileStmt) {
        while(isTruthy(evaluateExprStmt(whileStmt.condition))){
            evaluate(whileStmt.body);
        }
    }

    private void evaluateIfStmt(Stmt.If ifStmt) {
        int conditions = ifStmt.condition.size();
        for (int i = 0; i < conditions; i++){ 
            if(isTruthy(evaluateExprStmt(ifStmt.condition.get(i)))){
                evaluate(ifStmt.thenBranch.get(i));
                return;
            }
        }
        if(ifStmt.elseBranch != null){
            evaluate(ifStmt.elseBranch);
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
            case Expr.Logical logical -> {
                return evaluateLogicalExpr(logical);
            }
            case Expr.List_ list -> {
                return evaluateListExpr(list);
            }
            case Expr.Index index -> {
                return evaluateIndexExpr(index);
            }
            case Expr.Call call -> {
                return evaluateCallExpr(call);
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
        if (right instanceof Double) return (double) right != 0;
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

    private Object evaluateCallExpr(Expr.Call call){
        Environment localEnvironment = new Environment(global);
        Object calle = environment.get(call.identifier);
        Function function = (Function) calle;
        List<Token> params = function.params;
        List<Expr> arguments = call.arguments;
        if(function.arity() != arguments.size()){
            throw new RuntimeError(call.identifier, "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
        }
        for (int i = 0; i < function.arity(); i++){
            localEnvironment.define(params.get(i).lexeme, evaluateExprStmt(arguments.get(i)));
        }
        try {
            evaluateBlockStmt(new Stmt.Block(function.body), localEnvironment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }

    private Object evaluateIndexExpr(Expr.Index expr){
        Object value = environment.get(expr.identifier);
        
        if(!(value instanceof List<?> || value instanceof String)){
            throw new RuntimeError(expr.identifier, "Only lists and strings can be indexed.");
        }
        
        Object start = evaluateExprStmt(expr.start);
        Object end = expr.end == null ? null : evaluateExprStmt(expr.end);
        Object step = expr.step == null ? 1.0 : evaluateExprStmt(expr.step);

        // Ensure that start, end, and step are numeric
        if (!(start instanceof Double) || (end != null && !(end instanceof Double)) || !(step instanceof Double)) {
            throw new RuntimeError(expr.identifier, "Start, end, and step values must be numbers.");
        }

        int startIndex = ((Double) start).intValue();
        int endIndex = end == null ? startIndex+1 : ((Double) end).intValue();
        int stepValue = ((Double) step).intValue();

        if(value instanceof List<?> list){
            int size = list.size();
            if(startIndex < 0 || startIndex >= size || endIndex > size){
                throw new RuntimeError(expr.identifier, "Index out of bounds.");
            }
            List<Object> subList = new ArrayList<>();
            for (int i = startIndex; i < endIndex; i += stepValue){
                subList.add(list.get(i));
            }
            if(subList.size() == 1){
                return subList.get(0);
            }
            return subList;
        }
        else if(value instanceof String string){
            int size = string.length();
            if(startIndex < 0 || startIndex >= size || endIndex > size){
                throw new RuntimeError(expr.identifier, "Index out of bounds.");
            }
            StringBuilder subString = new StringBuilder();
            for (int i = startIndex; i < endIndex; i += stepValue){
                subString.append(string.charAt(i));
            }
            return subString.toString();
        }
        return null;
    }

    public Object evaluateListExpr(Expr.List_ expr) {
        List<Object> evalutedElements = new ArrayList<>();

        for (Expr element : (List<Expr>) expr.elements){
            evalutedElements.add(evaluateExprStmt(element));
        }
        return evalutedElements;
    }

    public Object evaluateLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    public Object evaluateLogicalExpr(Expr.Logical expr){
        Object left = evaluateExprStmt(expr.left);

        if(expr.operator.type == TokenType.OR){
            if(isTruthy(left)) return left;
        } else {
            if(!isTruthy(left)) return left;
        }
        return evaluateExprStmt(expr.right);
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