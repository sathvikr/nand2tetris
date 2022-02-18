import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        File[] files;
        File root = new File("projects/07/StackArithmetic/SimpleAdd/SimpleAdd.vm");
        CodeWriter codeWriter = new CodeWriter("projects/07/StackArithmetic/SimpleAdd/SimpleAdd.asm");

        if (root.isDirectory()) {
            files = root.listFiles();
        } else {
            files = new File[]{root};
        }

        assert files != null;
        for (File file : files) {
            if (file.getPath().substring(file.getPath().indexOf(".")).equals(".vm")) {
                Parser parser = new Parser(file);

                while (parser.hasMoreCommands()) {
                    parser.advance();

                    switch (parser.getCommandType()) {
                        case C_ARITHMETIC -> codeWriter.writeArithmetic(parser.getCurrentCommand());
                        case C_PUSH, C_POP -> codeWriter.writePushPop(parser.getCommandType(), parser.getArg1(), parser.getArg2());
                        default -> {
                        }
                    }
                }
            }
        }

        codeWriter.close();
    }
}
