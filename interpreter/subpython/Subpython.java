package interpreter.subpython;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Subpython {
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        // if(args.length > 1){
        //     System.err.println("Usage : subpython [script]");
        //     System.exit(64);
        // }
        // else if(args.length == 1){
        //     runFile(args[0]);
        // }
        // else{
        //     runPrompt();
        // }
        runFile("test.py");
    }

    private static void runFile(String path) throws IOException{
        String content = Files.readString(Paths.get(path));
        if(hadError) System.exit(65);
        if(hadRuntimeError) System.exit(70);
        run(content);
    }

    // 
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        for (;;) { 
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    public static void run(String source){
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        List<Stmt> statments = new Parser(tokens).parse();
        if(hadError) return;
        
        interpreter.interpreter(statments);
        // Print the tokens.
        // for (Token token : tokens) {
        //     System.out.println(token);
        // }

        // Print the AST.
        // System.out.println(new AstPrinter().print(expression));
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line,String where,String message){
        System.err.println("[line " + line + "] Error" + where + ": " + message);
    }

    static void error(Token token, String message) {
        hadError = true;
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}