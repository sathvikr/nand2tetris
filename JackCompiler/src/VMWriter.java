import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class VMWriter {
    private final FileWriter fileWriter;

    public VMWriter(File outfile) throws IOException {
        fileWriter = new FileWriter(outfile);
    }

    public void writePush(VirtualSegment segment, int index) throws IOException {
        fileWriter.write("push " + segment.toString().toLowerCase() + " " + index + "\n");
    }

    public void writePop(VirtualSegment segment, int index) throws IOException {
        fileWriter.write("pop " + segment.toString().toLowerCase() + " " + index + "\n");
    }

    public void writeArithmetic(String command) throws IOException {
        fileWriter.write(command + "\n");
    }

    public void writeLabel(String label) throws IOException {
        fileWriter.write("label " + label + "\n");
    }

    public void writeGoto(String label) throws IOException {
        fileWriter.write("goto " + label + "\n");
    }

    public void writeIf(String label) throws IOException {
        fileWriter.write("if-goto " + label + "\n");
    }

    public void writeCall(String name, int nArgs) throws IOException {
        fileWriter.write("call " + name + " " + nArgs + "\n");
    }

    public void writeFunction(String name, int nLocals) throws IOException {
        fileWriter.write("function " + name + " " + nLocals + "\n");
    }

    public void writeReturn() throws IOException {
        fileWriter.write("return\n");
    }

    public void close() throws IOException {
        fileWriter.close();
    }
}
