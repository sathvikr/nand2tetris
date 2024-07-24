import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private int runningIndex;
    private Map<String, VarInfo> classTable;
    private Map<String, VarInfo> subroutineTable;

    public SymbolTable() {
        classTable = new HashMap<>();
        startSubroutine();
    }

    public int getRunningIndex() {
        return runningIndex;
    }

    public void startSubroutine() {
        runningIndex = 0;
        subroutineTable = new HashMap<>();
    }

    public void define(String name, String type, VarKind kind) {
        if (kind.equals(VarKind.VAR) || kind.equals(VarKind.ARG)) {
            subroutineTable.put(name, new VarInfo(type, kind, runningIndex));
        } else {
            classTable.put(name, new VarInfo(type, kind, runningIndex));
        }

        runningIndex++;
    }

    public int varCount(VarKind kind) {
        int count = 0;
        Map<String, VarInfo> table = kind.equals(VarKind.VAR) || kind.equals(VarKind.ARG) ? subroutineTable : classTable;

        for (VarInfo varInfo : table.values()) {
            if (varInfo.getKind().equals(kind)) {
                count++;
            }
        }

        return count;
    }

    public VarKind kindOf(String name) {
        if (subroutineTable.containsKey(name)) {
            return subroutineTable.get(name).getKind();
        } else if (classTable.containsKey(name)) {
            return classTable.get(name).getKind();
        }

        return null;
    }

    public String typeOf(String name) {
        if (subroutineTable.containsKey(name)) {
            return subroutineTable.get(name).getType();
        } else if (classTable.containsKey(name)) {
            return classTable.get(name).getType();
        }

        return "";
    }

    public int indexOf(String name) {
        if (subroutineTable.containsKey(name)) {
            return subroutineTable.get(name).getIndex();
        } else if (classTable.containsKey(name)) {
            return classTable.get(name).getIndex();
        }

        return -1;
    }

    public String toString() {
        return classTable.toString() + "\n" + subroutineTable.toString();
    }
}
