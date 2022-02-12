import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Parser parser = new Parser(new File("projects/07/MemoryAccess/StaticTest/StaticTest.vm"));
        CodeWriter codeWriter = new CodeWriter("projects/07/VM Translator/Output.asm");

        while (parser.hasMoreCommands()) {
            parser.advance();

            switch (parser.getCommandType()) {
                case C_ARITHMETIC -> codeWriter.writeArithmetic(parser.getCurrentCommand());
                case C_PUSH, C_POP -> codeWriter.writePushPop(parser.getCommandType(), parser.getArg1(), parser.getArg2());
                default -> {}
            }
        }

        codeWriter.close();
    }
}
