public class VarInfo {
    private String type;
    private VarKind kind;
    private int index;

    public VarInfo(String type, VarKind kind, int index) {
        this.type = type;
        this.kind = kind;
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public VarKind getKind() {
        return kind;
    }

    public void setKind(VarKind kind) {
        this.kind = kind;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}
