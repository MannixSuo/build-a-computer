public enum Symbol {
    OPEN_BRACE('{'),
    CLOSE_BRACE('}'),
    OPEN_PAREN('('),
    CLOSE_PAREN(')'),
    OPEN_BRACKET('['),
    CLOSE_BRACKET(']'),
    PERIOD('.'),
    PLUS('+'),
    MINUS('-'),
    MULTIPLY('*'),
    DIVIDE('/'),
    COMMA(','),
    SEMICOLON(';'),
    AND('&'),
    VERTICAL_BAR('|'),
    LESS_THAN('<'),
    GREATER_THAN('>'),
    EQUAL('='),
    TILDE('~');
    private char value;
    Symbol(char value) {
        this.value = value;
    }

    public static boolean contain(char check){
        for (Symbol c :Symbol.values()){
            if (c.value==check){
                return true;
            }
        }
        return false;
    }

    public char getValue() {
        return value;
    }

    public void setValue(char value) {
        this.value = value;
    }

    public static char getSymbol(char c){
        for (Symbol s:values()){
            if (s.value==c){
                return s.value;
            }
        }
        return ' ';
    }
}
