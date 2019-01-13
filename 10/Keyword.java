public enum  Keyword {
    CLASS("class"),
    METHOD("method"),
    FUNCTION("function"),
    CONSTRUCTOR("constructor"),
    INT("int"),
    BOOLEAN("boolean"),
    CHAR("char"),
    VAR("var"),
    VOID("void"),
    STATIC("static"),
    FIELD("field"),
    LET("let"),
    DO("do"),
    IF("if"),
    ELSE("else"),
    WHILE("while"),
    RETURN("return"),
    TRUE("true"),
    FALSE("false"),
    THIS("this");
    String value;

    Keyword(String value) {
        this.value = value;
    }

    public static boolean contains(String value){
        for (Keyword keyword:Keyword.values()){
            if (keyword.value.equals(value)){
                return true;
            }
        }
        return false;
    }
    public static Keyword getKeyword(String value){
        for(Keyword key:values()){
            if (key.value.equals(value)){
                return key;
            }
        }
        return null;
    }
}
