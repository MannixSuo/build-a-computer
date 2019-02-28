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

    public void addSubroutineLevelSymbol(String name, String type, String kind) {
        Node node = new Node(name, type, kind, varCount(kind), SUBROUTINE_LEVEL);
        nodes.put(name, node);
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
       return (int) nodes.values().stream().filter(node -> node.getKind().equals(kind)).count();
    }


}
