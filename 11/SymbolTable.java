import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private static String CLASS_LEVEL = "CLASS";
    private static String SUBROUTINE_LEVEL = "SUBROUTINE";

    private SymbolTable previousTable;

    private Map<String, Node> nodes = new HashMap<>();

    private SymbolTable() {
    }

    private SymbolTable(SymbolTable previousTable) {
        this.previousTable = previousTable;
    }


    public SymbolTable startSubroutine(){
        return new SymbolTable(this);
    }

    public static SymbolTable createClassLevelTable() {
        return new SymbolTable();
    }


    public void addClassLevelSymbol(String name, String type, String kind) {
        Node node = new Node(name, type, kind, varCount(kind), CLASS_LEVEL);
        nodes.put(name, node);
    }

    public void addSubroutineevelSymbol(String name, String type, String kind) {
        Node node = new Node(name, type, kind, varCount(kind), CLASS_LEVEL);
        nodes.put("name", node);
    }

    public Node getSymbol(String name) {
        if (nodes.containsKey(name)) {
            return nodes.get(name);
        } else if (previousTable != null) {
            // if not fined in current table find in previous table
            return previousTable.getSymbol(name);
        } else {
            return null;
        }
    }

    private int varCount(String kind){
       return (int) nodes.values().stream().filter(node -> node.kind.equals(kind)).count();
    }

    private class Node {
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
}
