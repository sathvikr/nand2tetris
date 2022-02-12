import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Parser {
    private final Scanner sc;
    private String currentCommand;
    private String[] currentCommandArr;

    public Parser(File infile) throws FileNotFoundException {
        sc = new Scanner(infile);
    }

    public String getCurrentCommand() {
        return currentCommand;
    }

    public boolean hasMoreCommands() {
        return sc.hasNextLine();
    }

    public void advance() {
        currentCommand = sc.nextLine();
        currentCommandArr = currentCommand.split(" ");
    }

    public CommandType getCommandType() {
        return switch (currentCommandArr[0]) {
            case "add", "sub", "eq", "lt", "gt", "neg", "not", "and", "or" -> CommandType.C_ARITHMETIC;
            case "push" -> CommandType.C_PUSH;
            case "pop" -> CommandType.C_POP;
            default -> CommandType.C_INVALID;
        };
    }

    public String getArg1() {
        if (currentCommandArr.length == 1) {
            return currentCommandArr[0];
        }

        return currentCommandArr[1];
    }

    public int getArg2() {
        return Integer.parseInt(currentCommandArr[2]);
    }
}
