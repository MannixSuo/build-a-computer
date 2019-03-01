public class Node {

    public static String KIND_FIELD = "field";
    public static String KIND_ARGUMENT = "argument";
    public static String KIND_STATIC = "static";
    public static String KIND_LOCAL = "local";

    private String name;
    private String type;//(int, char, boolean, class name)
    private String kind;//(field, static, local, argument)
    private int index;
    private String scope; // class level,subroutine level

    public Node(String name, String type, String kind, int index, String scope) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.index = index;
        this.scope = scope;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
