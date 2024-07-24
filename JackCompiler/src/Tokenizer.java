import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Tokenizer {
    private static final String keywords = "class|constructor|function|method|field|static|var|int|char|boolean|void|" +
            "true|false|null|this|let|do|if|else|while|return";
    private static final String symbols = "[{}().,;+*-/&|<>=~]|\\[|\\]";

    private final Map<String, String> alteredSymbols = new HashMap<>();
    private final String content;
    private int currentIndexInLine;
    private String currentChar;
    private String currentToken;

    public Tokenizer(File infile) throws IOException {
        content = readFile(infile.getPath()).trim();
        currentIndexInLine = -1;
        currentChar = "";
        currentToken = "";

        alteredSymbols.put("<", "&lt");
        alteredSymbols.put(">", "&gt");
        alteredSymbols.put("\"", "&quot");
        alteredSymbols.put("&", "&amp");

        advance();
    }

    public Map<String, String> getAlteredSymbols() {
        return alteredSymbols;
    }

    private static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));

        return new String(encoded);
    }

    public boolean hasMoreTokens() {
        return currentIndexInLine <= content.length();
    }

    public void nextChar() {
        currentIndexInLine++;

        if (currentIndexInLine < content.length()) {
            currentChar = content.charAt(currentIndexInLine) + "";
        }
    }

    public void advance() {
        currentToken = "";

        while (true) {
            if (currentChar.trim().equals("")) {
                if (!currentToken.equals("")) {
                    break;
                }

                nextChar();
            } else if (currentChar.matches(symbols)) {
                if (!currentToken.equals("")) {
                    break;
                }

                currentToken += currentChar;
                nextChar();
                break;
            } else if (currentChar.equals("\"")) {
                currentToken += currentChar;
                nextChar();

                while (!currentChar.equals("\"")) {
                    currentToken += currentChar;
                    nextChar();
                }

                currentToken += currentChar;
                nextChar();
                break;
            } else if (currentChar.matches("[a-zA-Z0-9_]")) {
                if (!currentToken.equals("") && (currentToken.charAt(0) + "").matches("[0-9]")) {
                    if (currentChar.matches("[a-zA-Z_]")) {
                        // throw error? breaks out of loop on things like 9abc and splits into 9, abc for now
                        break;
                    }
                }
                currentToken += currentChar;
                nextChar();
            } else {
                break;
            }

        }
    }

    public TokenType tokenType() {
        if (currentToken.matches(symbols)) {
            return TokenType.SYMBOL;
        } else if (currentToken.matches("\".*\"")) {
            return TokenType.STRING_CONST;
        } else if (currentToken.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            if (currentToken.matches(keywords)) {
                return TokenType.KEYWORD;
            } else {
                return TokenType.IDENTIFIER;
            }
        } else if (currentToken.matches("[0-9]+")) {
            return TokenType.INT_CONST;
        }

        return null;
    }

    public Keyword keyword() {
        return Keyword.valueOf(currentToken.toUpperCase());
    }

    public char symbol() {
        return currentToken.charAt(0);
    }

    public String identifier() {
        return currentToken;
    }

    public int intVal() {
        return Integer.parseInt(currentToken);
    }

    public String stringVal() {
        return currentToken.substring(1, currentToken.length() - 1);
    }

    public String getCurrentToken() {
        if (tokenType().equals(TokenType.STRING_CONST)) {
            return currentToken.substring(1, currentToken.length() - 1);
        }

        return currentToken;
    }

}