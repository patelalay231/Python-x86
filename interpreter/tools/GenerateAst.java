package src.interpreter.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(1);
        }
        String baseName = args[0];
        String outputDir = args[1];
        defineAst(outputDir, "Expr", Arrays.asList(
            "Binary   : Expr left, Token operator, Expr right",
            "Grouping : Expr expression",
            "Literal  : Object value",
            "Unary    : Token operator, Expr right",
            "Assignment   : Token name, Expr value",
            "Variable : Token name",
            "Logical  : Expr left, Token operator, Expr right",
            "List_    : List<Expr> elements",
            "Index    : Token identifier, Expr start, Expr end, Expr step"
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
            "Expression : Expr expression",
            "If         : List<Expr> condition, List<Stmt> thenBranch, Stmt elseBranch",
            "Print      : Expr expression",
            "Assignment : Token name, Expr initializer",
            "Block      : List<Stmt> statements",
            "While      : Expr condition, Stmt body",
            "For        : Token name, Expr start, Expr end, Expr step, Stmt body",
            "ForIterable : Token name, Expr iterable, Stmt body"
        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package interpreter.subpython;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println("}");
        writer.close();
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println("    static class " + className + " extends " + baseName + " {");

        // Fields
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            writer.println("        final " + field + ";");
        }

        // Constructor
        writer.println();
        writer.println("        " + className + "(" + fieldList + ") {");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("            this." + name + " = " + name + ";");
        }
        writer.println("        }");

        writer.println("    }");
        writer.println();
    }
}
