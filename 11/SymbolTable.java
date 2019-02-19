import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    private SymbolTable previousTable;

    private SymbolTable() {
    }
    private SymbolTable(SymbolTable previousTable) {
        this.previousTable = previousTable;
    }

    private Map<String,Node> nodes = new HashMap<>();

    public SymbolTable createClassLevelTable(String name,String type,String kind,int index){
        return new SymbolTable();
    }

    public SymbolTable createSubroutineLevelTable(SymbolTable previousTable){
        return new SymbolTable(previousTable);
    }

    public void addSymbol(String name,String type,String kind,int index,String scope){
        Node node = new Node(name,type,kind,index,scope);
        nodes.put("name",node);
    }

    public Node getSymbol(String name){
        return nodes.getOrDefault(name,null);
    }

    private class Node{
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
