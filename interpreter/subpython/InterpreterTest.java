package interpreter.subpython;

import static interpreter.subpython.Subpython.hadError;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;


public class InterpreterTest {

    private static int passedTests = 0;
    private static int totalTests = 0;
    private static final Interpreter interpreter = new Interpreter();
    
    public static void main(String[] args) {
        String testFilePath = args[0]; // Fix the typo in the file name
        runTests(testFilePath);
    }

    private static void runTests(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] test = line.split(",");
                if (test.length != 2) continue; // Skip invalid lines
                
                String expression = test[0].trim();
                String expectedOutput = test[1].trim();

                if (runTest(expression, expectedOutput)) {
                    passedTests++;
                }
                totalTests++;
            }
        } catch (IOException e) {
            System.out.println("Error reading test file: " + e.getMessage());
        }

        System.out.println("Tests passed: " + passedTests + " / " + totalTests);
    }

    private static boolean runTest(String source, String expectedOutput) {
        hadError = false;

        try {
            String actualOutput = runAndCaptureOutput(source);
            if (actualOutput.equals(expectedOutput)) {
                System.out.println("PASS: " + source + " = " + expectedOutput);
                return true;
            } else {
                System.out.println("FAIL: " + source + ", expected " + expectedOutput + " but got " + actualOutput);
                return false;
            }
        } catch (Exception e) {
            System.out.println("ERROR: Exception running test for expression: " + source);
            return false;
        }
    }

    private static String runAndCaptureOutput(String source) {
        // Create a stream to capture output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out; // Save the original System.out

        // Redirect System.out to the output stream
        System.setOut(new PrintStream(outputStream));

        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        List<Stmt> statements = new Parser(tokens).parse();
        // Check if there were any errors in scanning/parsing
        if (hadError) {
            System.setOut(originalOut); // Restore original System.out
            return "Error scanning/parsing the expression: " + source;
        }

        // Interpret the statements
        interpreter.interpreter(statements);

        // Restore original System.out
        System.setOut(originalOut);

        // Return the captured output as a string
        return outputStream.toString().trim();
    }
}
