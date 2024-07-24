import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class CompilationEngine {
    private static final String type = "int|char|boolean|[a-zA-Z_][a-zA-Z0-9_]*";
    private static final String statement = "while|if|return|let|do";
    private static final String op = "[-+*/&|><=]";
    private static final String unaryOp = "[-~]";

    private final VMWriter vmWriter;
    private final FileWriter fileWriter;
    private final Tokenizer tokenizer;

    private int indents;
    private int numLabels;

    private SymbolTable symbolTable;
    private String className;

    public CompilationEngine(File infile, File outfile, File xmlFile) throws IOException {
        this.vmWriter = new VMWriter(outfile);
        this.fileWriter = new FileWriter(xmlFile);
        tokenizer = new Tokenizer(infile);
        indents = 0;
        numLabels = 0;

        compileClass();
    }

    private void writeLine(String line) throws IOException {
        for (int i = 0; i < indents; i++) {
            fileWriter.write("\t");
        }

        fileWriter.write(line + "\n");
    }

    public void writeIdentifier(String identifier, String kind) throws IOException {
        int runningIndex = symbolTable.getRunningIndex();

        writeLine("<" + kind + "Identifier" + runningIndex + "> " + identifier + " </" + kind + "Identifier" + runningIndex + ">");
    }

    public String getCurrentTokenString() {
        switch (tokenizer.tokenType()) {
            case KEYWORD -> {
                return "<keyword> " + tokenizer.getCurrentToken() + " </keyword>";
            }
            case SYMBOL -> {
                if ((tokenizer.symbol() + "").matches("[<>\"&]")) {
                    return "<symbol> " + tokenizer.getAlteredSymbols().get(tokenizer.symbol() + "") + "; </symbol>";
                } else {
                    return "<symbol> " + tokenizer.symbol() + " </symbol>";
                }
            }
            case IDENTIFIER -> {
                return "<" + "identifier> " + tokenizer.identifier() + " </" + "identifier>";
            }
            case INT_CONST -> {
                return "<integerConstant> " + tokenizer.intVal() + " </integerConstant>";
            }
            case STRING_CONST -> {
                return "<stringConstant> " + tokenizer.stringVal() + "</stringConstant>";
            }
        }

        return "";
    }

    private void advanceIdentifier(String kind) throws IOException {
        writeIdentifier(tokenizer.identifier(), kind);
        tokenizer.advance();
    }

    private void advance() throws IOException {
        writeLine(getCurrentTokenString());
        tokenizer.advance();
    }

    private String eatIdentifier(String kind) throws IOException {
        String eatenIdentifier = tokenizer.getCurrentToken();

        if (tokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
            advanceIdentifier(kind);
        } else {
            System.exit(0);
        }

        return eatenIdentifier;
    }

    private String eat(String str) throws IOException {
        String eatenToken = tokenizer.getCurrentToken();

        if (tokenizer.getCurrentToken().matches(str)) {
            advance();
        } else {
            System.exit(0);
        }

        return eatenToken;
    }

    private String symbolToUnaryOp(String symbol) {
        return switch (symbol) {
            case "-" -> "neg";
            case "~" -> "not";
            default -> null;
        };
    }

    private String symbolToBinOp(String symbol) {
        return switch (symbol) {
            case "+" -> "add";
            case "-" -> "sub";
            case "=" -> "eq";
            case ">" -> "gt";
            case "<" -> "lt";
            case "*" -> "call Math.mult 2";
            case "/" -> "call Math.div 2";
            default -> null;
        };
    }

    private VirtualSegment getVirtualSegment(VarKind kind) {
        return switch (kind) {
            case STATIC -> VirtualSegment.STATIC;
            case FIELD, VAR -> VirtualSegment.LOCAL;
            case ARG -> VirtualSegment.ARG;
            default -> throw new IllegalStateException("Unexpected value: " + kind);
        };
    }

    public void compileClass() throws IOException {
        symbolTable = new SymbolTable();

        writeLine("<class>");
        indents++;

        eat("class");
        className = eatIdentifier("class");
        eat("\\{");

        while (tokenizer.getCurrentToken().matches("static|field")) {
            compileClassVarDec();
        }

        while (tokenizer.getCurrentToken().matches("constructor|function|method")) {
            compileSubroutineDec();
        }

        eat("\\}");

        indents--;
        writeLine("</class>");
    }

    public void compileClassVarDec() throws IOException {
        writeLine("<classVarDec>");
        indents++;

        String kind = eat("static|field");
        VarKind varKind = VarKind.valueOf(kind.toUpperCase());
        String varType = eat(type);
        String varName = eatIdentifier(kind);

        symbolTable.define(varName, varType, varKind);

        while (tokenizer.getCurrentToken().equals(",")) {
            advance();
            varName = eatIdentifier(kind);
            symbolTable.define(varName, varType, varKind);
        }

        eat(";");

        indents--;
        writeLine("</classVarDec>");
    }

    public void compileSubroutineDec() throws IOException {
        symbolTable.startSubroutine();

        writeLine("<subroutineDec>");
        indents++;

        String subroutineType = eat("constructor|function|method");
        eat("void|" + type);
        eatIdentifier("subroutine");

        eat("\\(");
        compileParameterList(subroutineType);
        eat("\\)");
        compileSubroutineBody();

        indents--;
        writeLine("</subroutineDec>");
    }

    public void compileParameterList(String subroutineType) throws IOException {
        writeLine("<parameterList>");
        indents++;

        if (subroutineType.equals("constructor") || subroutineType.equals("method")) {
            writeIdentifier("this", "argument");
            symbolTable.define("this", className, VarKind.ARG);
        }

        if (!tokenizer.getCurrentToken().equals(")")) {
            String varType = eat(type);
            String varName = eatIdentifier("argument");

            symbolTable.define(varName, varType, VarKind.ARG);

            while (tokenizer.getCurrentToken().equals(",")) {
                advance();

                varType = eat(type);
                varName = eatIdentifier("argument");

                symbolTable.define(varName, varType, VarKind.ARG);
            }
        }

        indents--;
        writeLine("</parameterList>");
    }

    public void compileSubroutineBody() throws IOException {
        writeLine("<subroutineBody>");
        indents++;

        eat("\\{");

        while (tokenizer.getCurrentToken().equals("var")) {
            compileVarDec();
        }

        compileStatements();
        eat("\\}");

        indents--;
        writeLine("</subroutineBody>");
    }

    public void compileVarDec() throws IOException {
        writeLine("<varDec>");
        indents++;

        eat("var");
        String varType = eat(type);
        String varName = eatIdentifier("var");

        symbolTable.define(varName, varType, VarKind.VAR);

        while (tokenizer.getCurrentToken().equals(",")) {
            advance();
            varName = eatIdentifier("var");

            symbolTable.define(varName, varType, VarKind.VAR);
        }

        eat(";");

        indents--;
        writeLine("</varDec>");
    }

    public void compileStatements() throws IOException {
        writeLine("<statements>");
        indents++;

        while (tokenizer.getCurrentToken().matches(statement)) {
            switch (tokenizer.getCurrentToken()) {
                case "if" -> compileIf();
                case "let" -> compileLet();
                case "while" -> compileWhile();
                case "do" -> compileDo();
                case "return" -> compileReturn();
            }
        }

        indents--;
        writeLine("</statements>");
    }

    public void compileIf() throws IOException {
        writeLine("<ifStatement>");
        indents++;

        eat("if");
        eat("\\(");
        compileExpression();
        eat("\\)");

        vmWriter.writeArithmetic("not");
        vmWriter.writeIf("L" + numLabels);
        int initialIf = numLabels;

        eat("\\{");
        compileStatements();
        eat("\\}");

        if (tokenizer.getCurrentToken().equals("else")) {
            advance();

            numLabels++;
            vmWriter.writeGoto("L" + numLabels);
            vmWriter.writeLabel("L" + initialIf);

            eat("\\{");
            compileStatements();
            eat("\\}");
        }

        vmWriter.writeLabel("L" + numLabels);
        numLabels++;

        indents--;
        writeLine("</ifStatement>");
    }

    public void compileLet() throws IOException {
        writeLine("<letStatement>");
        indents++;

        eat("let");
        String identifier = tokenizer.getCurrentToken();
        VarKind varKind = symbolTable.kindOf(identifier);
        String kind = varKind.toString().toLowerCase();
        eatIdentifier(kind);

        if (tokenizer.getCurrentToken().equals("[")) {
            advance();
            compileExpression();
            eat("]");
        }

        eat("=");
        compileExpression();
        eat(";");

        vmWriter.writePop(getVirtualSegment(varKind), symbolTable.indexOf(identifier));

        indents--;
        writeLine("</letStatement>");
    }

    public void compileWhile() throws IOException {
        writeLine("<whileStatement>");
        indents++;

        eat("while");

        vmWriter.writeLabel("L" + numLabels);
        int initialWhile = numLabels;
        numLabels++;

        eat("\\(");
        compileExpression();

        vmWriter.writeArithmetic("not");
        vmWriter.writeIf("L" + numLabels);

        eat("\\)");
        eat("\\{");
        compileStatements();
        eat("\\}");

        vmWriter.writeGoto("L" + initialWhile);
        vmWriter.writeLabel("L" + numLabels);

        indents--;
        writeLine("</whileStatement>");
        numLabels++;
    }

    public void compileDo() throws IOException {
        writeLine("<doStatement>");
        indents++;

        eat("do");

        if (tokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
            String identifier = tokenizer.getCurrentToken();
            tokenizer.advance();

            if (tokenizer.getCurrentToken().equals(".")) {
                VarKind kind = symbolTable.kindOf(identifier);

                if (kind == null) {
                    writeIdentifier(identifier, "class");
                } else {
                    writeIdentifier(identifier, kind.toString().toLowerCase());
                }

                advance();
                eatIdentifier("subroutine");
            } else {
                writeIdentifier(identifier, "subroutine");
            }
        }

        eat("\\(");
        compileExpressionList();
        eat("\\)");
        eat(";");

        indents--;
        writeLine("</doStatement>");
    }

    public void compileReturn() throws IOException {
        writeLine("<returnStatement>");
        indents++;

        eat("return");

        if (!tokenizer.getCurrentToken().equals(";")) {
            compileExpression();
        }

        eat(";");

        indents--;
        writeLine("</returnStatement>");
    }

    public void compileExpression() throws IOException {
        writeLine("<expression>");
        indents++;

        compileTerm();

        while (tokenizer.getCurrentToken().matches(op)) {
            String operator = tokenizer.getCurrentToken();

            advance();
            compileTerm();
            System.out.println(operator);
            vmWriter.writeArithmetic(symbolToBinOp(operator));
        }

        indents--;
        writeLine("</expression>");
    }

    public void compileTerm() throws IOException {
        writeLine("<term>");
        indents++;
        String initialToken = tokenizer.getCurrentToken();

        if (tokenizer.tokenType().equals(TokenType.INT_CONST) ||
                tokenizer.tokenType().equals(TokenType.STRING_CONST) ||
                tokenizer.tokenType().equals(TokenType.KEYWORD)) {

            if (tokenizer.tokenType().equals(TokenType.INT_CONST)) {
                vmWriter.writePush(VirtualSegment.CONSTANT, Integer.parseInt(initialToken));
            }

            advance();
        } else if (tokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
            VarKind varKind = symbolTable.kindOf(initialToken);

            eatIdentifier("");

            if (tokenizer.getCurrentToken().equals("[")) {
                advance();
                compileExpression();
                eat("\\]");
            } else if (tokenizer.getCurrentToken().matches("[.(]")) {
                if (tokenizer.getCurrentToken().equals(".")) {
                    advance();
                    eatIdentifier("");
                }

                eat("\\(");
                compileExpressionList();
                eat("\\)");
            } else {
                vmWriter.writePush(getVirtualSegment(varKind), symbolTable.indexOf(initialToken));
            }
        } else if (initialToken.equals("(")) {
            advance();
            compileExpression();
            eat("\\)");
        } else if (initialToken.matches(unaryOp)) {
            advance();
            compileTerm();
            vmWriter.writeArithmetic(symbolToUnaryOp(initialToken));
        }

        indents--;
        writeLine("</term>");
    }

    public void compileExpressionList() throws IOException {
        writeLine("<expressionList>");
        indents++;

        if (!tokenizer.getCurrentToken().equals(")")) {
            compileExpression();

            while (tokenizer.getCurrentToken().equals(",")) {
                advance();
                compileExpression();
            }
        }

        indents--;
        writeLine("</expressionList>");
    }

    public void close() throws IOException {
        fileWriter.close();
        vmWriter.close();
    }
}
