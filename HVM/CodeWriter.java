import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CodeWriter {
    static String[] pointers = {"THIS", "THAT"};
    String filename;
    FileWriter filewriter;

    public CodeWriter(String outfilePath) throws IOException {
        File f = new File(outfilePath);

        filewriter = new FileWriter(f);
        filename = f.getName().substring(0, f.getName().indexOf('.'));

        initializePointers();
    }

    public int getArgCount(String command) {
        return switch (command) {
            case "neg", "not" -> 1;
            case "add", "sub", "eq", "gt", "lt", "and", "or" -> 2;
            default -> -1;
        };
    }

    public String getOperator(String command) {
        return switch (command) {
            case "add" -> "+";
            case "neg", "sub" -> "-";
            case "and" -> "&";
            case "or" -> "|";
            case "not" -> "!";
            default -> "";
        };
    }

    private void initializePointer(String pointerName, int address) throws IOException {
        filewriter.write("@" + address + "\n");
        filewriter.write("D=A\n");
        filewriter.write("@" + pointerName + "\n");
        filewriter.write("M=D\n");
    }

    private void initializePointers() throws IOException {
        initializePointer("SP", 256);
        initializePointer("LCL", 300);
        initializePointer("ARG", 400);
        initializePointer("THIS", 3000);
        initializePointer("THAT", 3010);
    }

    public void writeArithmetic(String command) throws IOException {
        int argCount = getArgCount(command);
        String operator = getOperator(command);

        if (argCount == 1) {
            // SP--
            filewriter.write("@SP\n");
            filewriter.write("M=M-1\n");

            // D = {operator}RAM[SP]
            filewriter.write("@SP\n");
            filewriter.write("A=M\n");
            filewriter.write("D=" + operator + "M\n");
        } else if (argCount == 2) {
            // SP-=2
            filewriter.write("@SP\n");
            filewriter.write("M=M-1\n");
            filewriter.write("M=M-1\n");

            // D = RAM[SP]
            filewriter.write("A=M\n");
            filewriter.write("D=M\n");

            // D = D + RAM[SP+1]
            filewriter.write("@SP\n");
            filewriter.write("A=M+1\n");

            if (!operator.equals("")) {
                filewriter.write("D=D" + operator + "M\n");
            } else {
                long timestamp = System.nanoTime();

                filewriter.write("D=D-M\n");
                filewriter.write("@COND_" + timestamp + "\n");

                switch (command) {
                    case "eq" -> filewriter.write("D;JEQ\n");
                    case "gt" -> filewriter.write("D;JGT\n");
                    case "lt" -> filewriter.write("D;JLT\n");
                    default -> {}
                }

                filewriter.write("D=0\n");
                filewriter.write("@NOT_COND_" + timestamp + "\n");
                filewriter.write("0;JMP\n");
                filewriter.write("(COND_" + timestamp + ")\n");
                filewriter.write("D=-1\n");
                filewriter.write("(NOT_COND_" + timestamp + ")\n");
            }
        }

        // push answer of computation onto stack
        // RAM[SP] = D
        filewriter.write("@SP\n");
        filewriter.write("A=M\n");
        filewriter.write("M=D\n");

        // SP++
        filewriter.write("@SP\n");
        filewriter.write("M=M+1\n");
    }

    private String getSegmentBase(String segment) {
        return switch (segment) {
            case "local" -> "LCL";
            case "argument" -> "ARG";
            case "this" -> "THIS";
            case "that" -> "THAT";
            default -> "";
        };
    }

    public void writePushPop(CommandType commandType, String segment, int index) throws IOException {
        switch (segment) {
            case "local", "argument", "this", "that", "temp":
                // addr = segmentBase + index
                if (segment.equals("temp")) {
                    filewriter.write("@5\n");
                    filewriter.write("D=A\n");

                } else {
                    filewriter.write("@" + getSegmentBase(segment) + "\n");
                    filewriter.write("D=M\n");
                }

                filewriter.write("@" + index + "\n");
                filewriter.write("D=D+A\n");
                filewriter.write("@addr\n");
                filewriter.write("M=D\n");

                if (commandType == CommandType.C_PUSH) {
                    // RAM[SP] = RAM[addr]
                    filewriter.write("A=D\n");
                    filewriter.write("D=M\n");
                    filewriter.write("@SP\n");
                    filewriter.write("A=M\n");
                    filewriter.write("M=D\n");

                    // SP++
                    filewriter.write("@SP\n");
                    filewriter.write("M=M+1\n");
                } else if (commandType == CommandType.C_POP) {
                    // SP--
                    filewriter.write("@SP\n");
                    filewriter.write("M=M-1\n");

                    // RAM[addr] = RAM[SP]
                    filewriter.write("A=M\n");
                    filewriter.write("D=M\n");
                    filewriter.write("@addr\n");
                    filewriter.write("A=M\n");
                    filewriter.write("M=D\n");
                }

                filewriter.write("@addr\n");
                filewriter.write("M=0\n");

                break;
            case "constant":
                if (commandType == CommandType.C_PUSH) {
                    // RAM[SP] = index
                    filewriter.write("@" + index + "\n");
                    filewriter.write("D=A\n");
                    filewriter.write("@SP\n");
                    filewriter.write("A=M\n");
                    filewriter.write("M=D\n");

                    // SP++
                    filewriter.write("@SP\n");
                    filewriter.write("M=M+1\n");
                } else if (commandType == CommandType.C_POP) {
                    // SP--
                    filewriter.write("@SP\n");
                    filewriter.write("M=M-1\n");
                }

                break;
            case "static":
                if (commandType == CommandType.C_PUSH) {
                    // RAM[SP] = filename.index
                    filewriter.write("@" + filename + "." + index + "\n");
                    filewriter.write("D=M\n");
                    filewriter.write("@SP\n");
                    filewriter.write("A=M\n");
                    filewriter.write("M=D\n");

                    // SP++
                    filewriter.write("@SP\n");
                    filewriter.write("M=M+1\n");
                } else if (commandType == CommandType.C_POP) {
                    // SP--
                    filewriter.write("@SP\n");
                    filewriter.write("M=M-1\n");

                    // filename.index = RAM[SP]
                    filewriter.write("A=M\n");
                    filewriter.write("D=M\n");
                    filewriter.write("@" + filename + "." + index + "\n");
                    filewriter.write("M=D\n");
                }

                break;
            case "pointer":
                if (commandType == CommandType.C_PUSH) {
                    // RAM[SP] = THIS/THAT
                    filewriter.write("@" + pointers[index] + "\n");
                    filewriter.write("D=M\n");
                    filewriter.write("@SP\n");
                    filewriter.write("A=M\n");
                    filewriter.write("M=D\n");

                    // SP++
                    filewriter.write("@SP\n");
                    filewriter.write("M=M+1\n");
                } else if (commandType == CommandType.C_POP) {
                    // SP--
                    filewriter.write("@SP\n");
                    filewriter.write("M=M-1\n");

                    // THIS/THAT = RAM[SP]
                    filewriter.write("@SP\n");
                    filewriter.write("A=M\n");
                    filewriter.write("D=M\n");
                    filewriter.write("@" + pointers[index] + "\n");
                    filewriter.write("M=D\n");
                }

                break;
            default:
                break;
        }
    }

    public void close() throws IOException {
        filewriter.close();
    }
}
