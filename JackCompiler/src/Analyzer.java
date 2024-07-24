import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Analyzer {
    public static File[] getFiles(String path) {
        File[] files;
        File input = new File(path);

        if (input.isDirectory()) {
            files = input.listFiles();
        } else {
            files = new File[]{input};
        }

        return files;
    }

    public static boolean hasExtension(File file, String extension) {
        return file.getName().substring(file.getName().lastIndexOf(".")).equals(extension);
    }

    public static void main(String[] args) throws IOException {
        File[] filesToAnalyze = getFiles("/Users/sathvik.redrouthu/Desktop/nand2tetris/projects/JackCompiler/tests/Square/SquareGame.jack");
        CompilationEngine compilationEngine;

        for (File infile : filesToAnalyze) {
            if (hasExtension(infile, ".jack")) {
                String outfilePathBase = infile.getPath().substring(0, infile.getPath().lastIndexOf("."));
                File outfile = new File(outfilePathBase + ".vm");
                File xmlFile = new File(outfilePathBase + ".xml");

                compilationEngine = new CompilationEngine(infile, outfile, xmlFile);
                compilationEngine.close();
            }
        }
    }
}
