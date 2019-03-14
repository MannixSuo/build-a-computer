import java.io.File;
import java.io.IOException;

public class CompilationEngine {

    private JackTokenizer jackTokenizer;
    private VMWriter writer;
    private String className = "noClass";
    private int labelNumber;

    private static int FUNCTION_TYPE_FUNCTION = 0;
    private static int FUNCTION_TYPE_METHOD = 1;
    private static int FUNCTION_TYPE_CONSTRUCTOR = 2;

    public CompilationEngine(JackTokenizer jackTokenizer, File outputFile) throws IOException {
        this.jackTokenizer = jackTokenizer;
        this.writer = new VMWriter(outputFile);
    }

    public void writeOutputFile() throws IOException {
        writer.close();
    }

    public void compile() throws IOException {
        jackTokenizer.advance();
        TokenType tokenType = jackTokenizer.tokenType();
        if (tokenType == TokenType.KEYWORD) {
            Keyword keyword = jackTokenizer.keyword();
            if (keyword == Keyword.CLASS) {
                compileClass();
            } else {
                System.out.println("a class should start with keyword 'class'.");
            }
        }
    }

    public void compileStatements(SymbolTable subroutineSymbolTable) throws IOException {
        System.out.println("compile statements");
        // let if while do return
        char symbol = jackTokenizer.symbol();
        while (symbol != Symbol.CLOSE_BRACE.getValue()) {
            Keyword keyword = jackTokenizer.keyword();
            if (keyword == null) {
                break;
            }
            if (Keyword.LET.value.equals(keyword.value)) {
                compileLet(subroutineSymbolTable);
                jackTokenizer.advance();
            } else if (Keyword.WHILE.value.equals(keyword.value)) {
                compileWhile(subroutineSymbolTable);
                jackTokenizer.advance();
            } else if (Keyword.IF.value.equals(keyword.value)) {
                compileIf(subroutineSymbolTable);
            } else if (Keyword.DO.value.equals(keyword.value)) {
                compileDo(subroutineSymbolTable);
                jackTokenizer.advance();
            } else if (Keyword.RETURN.value.equals(keyword.value)) {
                compileReturn(subroutineSymbolTable);
            }
            symbol = jackTokenizer.symbol();
            //else if (keyword.value.equals(Keyword.))
        }
    }

    public void compileClass() throws IOException {
        System.out.println("compile class");
        // 'class' className '{' classVarDec* subroutineDec* '}'
        // tokenXmlBuilder.setStartNode(Keyword.CLASS.value);
        // 'class'
        SymbolTable classLevelSymbolTable = SymbolTable.createClassLevelTable();
        //  className
        jackTokenizer.advance();
        TokenType identifierType = jackTokenizer.tokenType();

        className = jackTokenizer.identifier();

        if (identifierType == TokenType.IDENTIFIER) {
        } else {
            System.out.println("class identifier did not appear");
            return;
        }
        // {
        jackTokenizer.advance();
        TokenType symbolType = jackTokenizer.tokenType();
        if (symbolType == TokenType.SYMBOL) {
            char symbol = jackTokenizer.symbol();
        } else {
            System.out.println("miss symbol '{' after class identifier");
            return;
        }
        jackTokenizer.advance();
        //  classVarDec* subroutineDec*
        while (Symbol.CLOSE_BRACE.getValue() != jackTokenizer.symbol()) {
            TokenType tokenType = jackTokenizer.tokenType();
            if (TokenType.KEYWORD.equals(tokenType)) {
                Keyword keyword = jackTokenizer.keyword();
                if (Keyword.FIELD.equals(keyword) || Keyword.STATIC.equals(keyword)) {
                    compileClassVarDec(classLevelSymbolTable);
                } else if (Keyword.FUNCTION.equals(keyword)) {
                    compileSubroutineDec(classLevelSymbolTable);
                } else if (Keyword.METHOD.equals(keyword)) {
                    compileSubroutineDec(classLevelSymbolTable);
                } else if (Keyword.CONSTRUCTOR.equals(keyword)) {
                    compileSubroutineDec(classLevelSymbolTable);
                } else {
                    System.out.println("unknown code : " + keyword.value);
                    return;
                }
            }
            jackTokenizer.advance();
        }
        // }
        //</class>
    }

    public void compileClassVarDec(SymbolTable classLevelSymbolTable) throws IOException {
        System.out.println("compile class var dec");
        // (static | field) type varName(,varName)* ';'
        Keyword varScopeKeyword = jackTokenizer.keyword();
        // (static | field)
        String kind = varScopeKeyword.value;
        jackTokenizer.advance();
        TokenType tokenType = jackTokenizer.tokenType();
        // type
        String varType = "";
        if (tokenType == TokenType.KEYWORD) {
            varType = jackTokenizer.keyword().value;
        } else if (TokenType.IDENTIFIER == tokenType) {
            varType = jackTokenizer.identifier();
        }
        jackTokenizer.advance();
        // varName
        String varName = jackTokenizer.identifier();

        classLevelSymbolTable.addClassLevelSymbol(varName, varType, kind);
        jackTokenizer.advance();
        char symbol = jackTokenizer.symbol();
        while (symbol != Symbol.SEMICOLON.getValue()) {
            // ,
            jackTokenizer.advance();
            // varName
            varName = jackTokenizer.identifier();

            classLevelSymbolTable.addClassLevelSymbol(varName, varType, kind);

            jackTokenizer.advance();
            symbol = jackTokenizer.symbol();
        }
    }

    public void compileSubroutineDec(SymbolTable classLevelSymbolTable) throws IOException {
        System.out.println("compile subroutine dec");
        // (constructor|function|method) (void|type) subroutineName (parameterList) subroutineBody
        SymbolTable subroutineSymbolTable = classLevelSymbolTable.startSubroutine();
        // (constructor|function|method)
        Keyword keyword = jackTokenizer.keyword();
        int functionType = 0;
        jackTokenizer.advance();

        // (void|type)
        TokenType keywordOrIdentifierType = jackTokenizer.tokenType();
        boolean voidMethod = false;
        boolean isMethod = false;
        if (keywordOrIdentifierType == TokenType.KEYWORD) {
            if (jackTokenizer.keyword().value.equals(Keyword.VOID)){
                voidMethod = true;
            }
        } else if (keywordOrIdentifierType == TokenType.IDENTIFIER) {
        }

        jackTokenizer.advance();
        // subroutineName
        TokenType tokenType = jackTokenizer.tokenType();
        String identifier = jackTokenizer.identifier();
        if (tokenType == TokenType.IDENTIFIER) {
        } else {
            System.out.println("miss identifier after subroutine keyword");
        }
        jackTokenizer.advance();
        // (
        char symbol = jackTokenizer.symbol();
        boolean isFunction =false;

        if (keyword.is(Keyword.METHOD)) {
            functionType = FUNCTION_TYPE_METHOD;
            // 'this' is the first parameter of a method
            subroutineSymbolTable.addSubroutineLevelSymbol("this", className, Node.KIND_ARGUMENT);
            isMethod = true;
        }

        Integer argNum = compileParameterList(subroutineSymbolTable);

        if(keyword.is(Keyword.CONSTRUCTOR)){
            functionType = FUNCTION_TYPE_CONSTRUCTOR;
            int field = classLevelSymbolTable.varCount("field");
            argNum = field;
            writer.writePushConst(field);
            writer.writeCall(" Memory.alloc",1);
            writer.popPointer(0);
        }

        if (keyword.is(Keyword.FUNCTION)){
            functionType = FUNCTION_TYPE_FUNCTION;
        }

        if (keyword.is(Keyword.METHOD)) {
            argNum++;
            writer.writePush("argument",0);
            writer.popPointer(0);
        }

        // )
        jackTokenizer.advance();
        compileSubroutineBody(subroutineSymbolTable,identifier,functionType);
    }

    public Integer compileParameterList(SymbolTable subroutineSymbolTable) throws IOException {
        System.out.println("compile parameter list");
        // (type varName (,type varName)*)?
        jackTokenizer.advance();
        Integer argNum = 0;
        while (jackTokenizer.symbol() != ')') {
            // type
            TokenType keywordOrIdentifier = jackTokenizer.tokenType();
            String type;
            if (keywordOrIdentifier == TokenType.KEYWORD) {
                Keyword keyword = jackTokenizer.keyword();
                type = keyword.value;
            } else if (keywordOrIdentifier == TokenType.IDENTIFIER) {
                type = jackTokenizer.identifier();
            } else {
                type = "null";
                System.out.println("missing parameter keyword error");
            }
            jackTokenizer.advance();
            // name
            TokenType identifierType = jackTokenizer.tokenType();
            if (identifierType == TokenType.IDENTIFIER) {
                String identifier = jackTokenizer.identifier();
                subroutineSymbolTable.addSubroutineLevelSymbol(identifier, type, Node.KIND_ARGUMENT);
            } else {
                System.out.println("error miss parameter identifier");
            }
            jackTokenizer.advance();
            TokenType commaOrEndParen = jackTokenizer.tokenType();
            if (commaOrEndParen == TokenType.SYMBOL) {
                char symbol = jackTokenizer.symbol();
                // ,
                if (symbol == Symbol.COMMA.getValue()) {
                    jackTokenizer.advance();
                }
            }
            argNum++;
        }
        return argNum;
    }

    public void compileSubroutineBody(SymbolTable subroutineSymbolTable, String identifier, int functionType) throws IOException {
        System.out.println("compile subroutine body");
        // '{' varDec* statements '}'
        TokenType symbolType = jackTokenizer.tokenType();
        if (symbolType == TokenType.SYMBOL) {
            char symbol = jackTokenizer.symbol();
            // {
        } else {
            System.out.println("miss { after parameterList");
        }
        jackTokenizer.advance();
        // subroutine end with '}'
        while (jackTokenizer.symbol() != Symbol.CLOSE_BRACE.getValue()) {
            TokenType tokenType = jackTokenizer.tokenType();
            if (tokenType == TokenType.KEYWORD) {
                Keyword keyword = jackTokenizer.keyword();
                if (keyword.value.equals(Keyword.VAR.value)) {
                    compileVarDec(subroutineSymbolTable);
                } else {
                    createFunction(subroutineSymbolTable,identifier,functionType);
                    compileStatements(subroutineSymbolTable);
                    // jackTokenizer.advance();
                }
            }
        }
        // }
    }

    private void createFunction(SymbolTable subroutineSymbolTable, String identifier, int functionType) throws IOException {
        int argNum = 0;
        if (FUNCTION_TYPE_CONSTRUCTOR==functionType){
            argNum = subroutineSymbolTable.varCount("field");
        }else if (FUNCTION_TYPE_METHOD==functionType){
            argNum = subroutineSymbolTable.varCount("argument") + 1;
        }else if (FUNCTION_TYPE_FUNCTION ==functionType){
            argNum = subroutineSymbolTable.varCount("local");
        }
        writer.writeFunction(className.concat(".").concat(identifier),argNum);
    }

    public void compileVarDec(SymbolTable subroutineSymbolTable) throws IOException {
        System.out.println("compile var dec");
        // var type varName(',' varName)* ';'
        // 'var'
        jackTokenizer.advance();
        TokenType tokenType = jackTokenizer.tokenType();
        // type
        String type;
        if (tokenType == TokenType.IDENTIFIER) {
            type = jackTokenizer.identifier();
        } else if (tokenType == TokenType.KEYWORD) {
            Keyword keyword = jackTokenizer.keyword();
            type = keyword.value;
        } else {
            type = "null";
            System.out.println("miss identifier or keyword after var keyword");
        }
        // var int a,b,c,d;
        jackTokenizer.advance();
        while (jackTokenizer.symbol() != Symbol.SEMICOLON.getValue()) {
            TokenType identifierOrSymbol = jackTokenizer.tokenType();
            if (identifierOrSymbol == TokenType.IDENTIFIER) {
                String identifier2 = jackTokenizer.identifier();
                subroutineSymbolTable.addSubroutineLevelSymbol(identifier2, type, Node.KIND_LOCAL);
            } else if (identifierOrSymbol == TokenType.SYMBOL) {
                char symbol = jackTokenizer.symbol();
                if (symbol == Symbol.COMMA.getValue()) {
                }
            }
            jackTokenizer.advance();
        }
        jackTokenizer.advance();
    }

    public void compileLet(SymbolTable subroutineSymbolTable) throws IOException {
        System.out.println("compile let");
        // 'let' varName ('[' expression ']')? '=' expression ';'

        // ....
        // pop varName

        // let
        jackTokenizer.advance();
        // varName
        String varName = jackTokenizer.identifier();
        Node symbol = subroutineSymbolTable.getSymbol(varName);

        jackTokenizer.advance();
        char symbol1 = jackTokenizer.symbol();
        if (symbol1 == Symbol.EQUAL.getValue()) {
            // =
            jackTokenizer.advance();
        } else if (symbol1 == Symbol.OPEN_BRACKET.getValue()) {
            // [
            jackTokenizer.advance();
            // expression
            compileExpression(subroutineSymbolTable);
            // ]
            jackTokenizer.advance();
            // =
            jackTokenizer.advance();
        }
        // expression
        compileExpression(subroutineSymbolTable);
        assert Symbol.SEMICOLON.getValue()==jackTokenizer.symbol();
        // ;
        // pop  kind index
        writer.writePopSymbol(symbol);
    }

    public void compileIf(SymbolTable subroutineSymbolTable) throws IOException {
        System.out.println("compile if");
        // 'if' '(' expression ')' '{' statements '}' ('else' '{' statements '}')?
        // if
        // if (
        jackTokenizer.advance();
        char symbol = jackTokenizer.symbol();
        // if( expression
        jackTokenizer.advance();
        compileExpression(subroutineSymbolTable);

        String label1 = String.format("L%d", generateLabel());
        String label2 = String.format("L%d", generateLabel());

        writer.writeLogic('~');
        writer.writeIf(label1);
        // if(expression)
        char symbol1 = jackTokenizer.symbol();
        jackTokenizer.advance();
        // if(expression) {
        char symbol2 = jackTokenizer.symbol();
        jackTokenizer.advance();
        // if(expression){ statements

        compileStatements(subroutineSymbolTable);

        writer.writeGoto(label2);
        writer.writeLabel(label1);
        // jackTokenizer.advance();
        // if(expression){statements }
        char symbol3 = jackTokenizer.symbol();
        jackTokenizer.advance();
        Keyword keyword = jackTokenizer.keyword();
        if (keyword == Keyword.ELSE) {

            jackTokenizer.advance();
            jackTokenizer.advance();

            compileStatements(subroutineSymbolTable);
            jackTokenizer.advance();

            writer.writeGoto(label2);
        } else {
            //jackTokenizer.moveBack();
        }
        writer.writeLabel(label2);
    }

    public void compileWhile(SymbolTable subroutineSymbolTable) throws IOException {
        System.out.println("compile while");
        // while
        jackTokenizer.advance();
        TokenType tokenType = jackTokenizer.tokenType();
        if (tokenType == TokenType.SYMBOL) {
            // while (
            char symbol = jackTokenizer.symbol();
        }
        jackTokenizer.advance();
        String label1 = String.format("L%d", generateLabel());
        String label2 = String.format("L%d", generateLabel());

        writer.writeLabel(label1);
        compileExpression(subroutineSymbolTable);
        writer.writeLogic('~');
        writer.writeIf(label2);
        char symbol = jackTokenizer.symbol();
        //while(expression )
        jackTokenizer.advance();
        char symbol1 = jackTokenizer.symbol();
        // while(expression) {
        // while(expression){ statements
        jackTokenizer.advance();
        compileStatements(subroutineSymbolTable);
        writer.writeGoto(label1);
        // while(expression){statements}
        char symbol3 = jackTokenizer.symbol();
        writer.writeLabel(label2);
    }

    public void compileDo(SymbolTable subroutineSymbolTable) throws IOException {
        System.out.println("compile do");
        // 'do' subroutineCall ';'
        // subroutineCall subroutineName '(' expressionList ')'
        //              | (classname|varName) '.' subroutineName '('expressionList')'
        // do
        // do a
        jackTokenizer.advance();
        String subOrClassOrVarName = jackTokenizer.identifier();
        // classname.subroutineName(expressionList)
        boolean isClass = false;
        // varName.subroutineName(expressionList)
        boolean isVar   = false;
        // subroutineName(expressionList)
        boolean isSub   = false;

        jackTokenizer.advance();
        String subroutineName = "subroutineName";
        char symbol = jackTokenizer.symbol();
        if (symbol == Symbol.PERIOD.getValue()) {
            // do a.
            jackTokenizer.advance();
            Node varNodeOrNull = subroutineSymbolTable.getSymbol(subOrClassOrVarName);
            if (varNodeOrNull != null){
                isVar = true;
                writer.writePushSymbol(varNodeOrNull);
            }else {
                isClass = true;
            }
            // do a.b
            subroutineName = jackTokenizer.identifier();
            jackTokenizer.advance();
        }else {
            isSub = true;
            writer.writePush("pointer",0);
        }
        // do a.b( || do a(
        char symbol1 = jackTokenizer.symbol();
        // do a.b(expressionList
        int argNum = compileExpressionList(subroutineSymbolTable);
        if (isSub||isVar) argNum++;
        String callName = "callName";
        if (isSub) callName = className.concat(".").concat(subOrClassOrVarName);
        if (isClass||isVar) callName = subroutineSymbolTable.typeOf(subOrClassOrVarName).concat(".").concat(subroutineName);

        writer.writeCall(callName,argNum);
        writer.writePop("temp",0);
        // do a.b(expressionList)
        char symbol2 = jackTokenizer.symbol();
        jackTokenizer.advance();
        // do a.b(expressionList) ;
        char symbol3 = jackTokenizer.symbol();
    }

    public void compileReturn(SymbolTable subroutineSymbolTable) throws IOException {
        // return expression? ;
        System.out.println("compile return statements");
        jackTokenizer.advance();
        char symbol = jackTokenizer.symbol();
        if (jackTokenizer.symbol() == Symbol.SEMICOLON.getValue()){
            writer.writePushConst(0);
        }
        while (jackTokenizer.symbol() != Symbol.SEMICOLON.getValue()) {
            compileExpression(subroutineSymbolTable);
            symbol = jackTokenizer.symbol();
        }
        writer.writeReturn();
        jackTokenizer.advance();
    }

    public void compileExpression(SymbolTable subroutineSymbolTable) throws IOException {
        System.out.println("compile expression");
        // term(op term)*
        // push term
        // push term
        // op

        // term
        compileTerm(subroutineSymbolTable);
        // op
        while (isOperationSymbol(jackTokenizer.symbol())) {
            char op = jackTokenizer.symbol();
            jackTokenizer.advance();
            // term
            compileTerm(subroutineSymbolTable);
            if (isArithmetic(op)){
                writer.writeArithmetic(op);
            }else {
                writer.writeLogic(op);
            }
        }
    }

    private boolean isArithmetic(char op) {
        return '+'==op||'-'==op||'*'==op||'/'==op;
    }

    public void compileTerm(SymbolTable subroutineSymbolTable) throws IOException {
        System.out.println("compile term");
        // integerConstant | stringConstant | keywordConstant |
        // varName | varName[expression] | subroutineCall | (expression) | unArrayOp term
        TokenType tokenType = jackTokenizer.tokenType();
        if (tokenType == TokenType.INT_CONST) {
            // integerConstant
            int i = jackTokenizer.intVal();
            jackTokenizer.advance();
            writer.writePushConst(i);
        } else if (tokenType == TokenType.STRING_CONST) {
            // stringConstant
            String stringVal = jackTokenizer.stringVal();
            int length = stringVal.length();
            writer.writePushConst(length);
            writer.writeCall("String.new",1);
            writer.popPointer(0);

            for (char c:stringVal.toCharArray()){
                writer.pushPointer(0);
                writer.writePushConst(c);
                writer.writeCall("String.appendChar",1);
                writer.popPointer(0);
            }
            jackTokenizer.advance();
        } else if (tokenType == TokenType.KEYWORD) {
            // keywordConstant
            String value = jackTokenizer.keyword().value;
            if (Keyword.THIS.is(jackTokenizer.keyword())){
                writer.pushPointer(0);
                jackTokenizer.advance();
            }else if (Keyword.TRUE.is(jackTokenizer.keyword())){
                writer.writePushConst(0);
                writer.writeLogic('~');
                jackTokenizer.advance();
            }else if (Keyword.FALSE.is(jackTokenizer.keyword())){
                writer.writePushConst(0);
                jackTokenizer.advance();
            }else if (Keyword.NULL.is(jackTokenizer.keyword())){
                writer.writePushConst(0);
                jackTokenizer.advance();
            }
        } else if (tokenType == TokenType.IDENTIFIER) {
            // varName
            String identifier = jackTokenizer.identifier();
            String type = subroutineSymbolTable.typeOf(identifier);
            Node caller = subroutineSymbolTable.getSymbol(identifier);
            boolean isMethod = false;
            if (caller != null){
                // not constructor
                writer.writePushSymbol(caller);
                isMethod = true;
            }
            // push xxx
            jackTokenizer.advance();
            TokenType nextType = jackTokenizer.tokenType();
            if (nextType == TokenType.SYMBOL) {
                // op or [ or ( or .
                char symbol = jackTokenizer.symbol();
                // subroutineCall
                // subroutineName '(' expressionList ')'
                // (className|varName) '.' subroutineName '(' expressionList ')'
                if (Symbol.PERIOD.getValue() == symbol) {
                    jackTokenizer.advance();
                    TokenType subIdentifier = jackTokenizer.tokenType();
                    if (subIdentifier == TokenType.IDENTIFIER) {
                        // a.b
                        String subIdentifierName = jackTokenizer.identifier();
                        jackTokenizer.advance();
                        char symbol1 = jackTokenizer.symbol();
                        if (symbol1 == Symbol.OPEN_PAREN.getValue()) {
                            // a.b(
                            // expressionList
                            Integer argNum = compileExpressionList(subroutineSymbolTable);
                            if (isMethod) argNum ++;
                            writer.writeCall(type.concat(".").concat(subIdentifierName),argNum);
                            // jackTokenizer.advance();
                            char symbol2 = jackTokenizer.symbol();
                            //a.b( expressionList )
                            jackTokenizer.advance();
                        }
                    }
                } else if (Symbol.OPEN_PAREN.getValue() == symbol) {
                    // a(
                    // a(expressionList
                    int argNum = compileExpressionList(subroutineSymbolTable);
                    writer.writeCall(identifier,argNum);
                    // a(expressionList)
                    char symbol1 = jackTokenizer.symbol();
                    jackTokenizer.advance();
                } else if (Symbol.OPEN_BRACKET.getValue() == symbol) {
                    // a[
                    // a[expression
                    // a[b[1]]
                    jackTokenizer.advance();
                    compileExpression(subroutineSymbolTable);
                    writer.writePushSymbol(subroutineSymbolTable.getSymbol(identifier));
                    writer.writeArithmetic('+');
                    // a[expression]
                    char symbol1 = jackTokenizer.symbol();
                    jackTokenizer.advance();
                }
            }
        } else if (tokenType == TokenType.SYMBOL) {
            char symbol = jackTokenizer.symbol();
            if (symbol == Symbol.OPEN_PAREN.getValue()) {
                // ( expression )
                jackTokenizer.advance();
                compileExpression(subroutineSymbolTable);
                jackTokenizer.advance();
            } else if (symbol == Symbol.TILDE.getValue() || symbol == Symbol.MINUS.getValue()) {
                // unArrayOp - ~
                jackTokenizer.advance();
                // term
                compileTerm(subroutineSymbolTable);
                writer.writeLogic(symbol);
            } else {
                System.out.println("term command not find: " + symbol);
            }
        }
    }

    public Integer compileExpressionList(SymbolTable subroutineSymbolTable) throws IOException {
        System.out.println("compile expression list");
        // (expression(, expression)*)?
        Integer argNum = 0;
        jackTokenizer.advance();
        while (Symbol.CLOSE_PAREN.getValue() != jackTokenizer.symbol()) {
            if (jackTokenizer.symbol() == Symbol.COMMA.getValue()) {
                jackTokenizer.advance();
                compileExpression(subroutineSymbolTable);
            } else {
                compileExpression(subroutineSymbolTable);
            }
            argNum++;
        }
        return argNum;
    }

    private boolean isOperationSymbol(char symbol) {
        // + - * / return true
        return symbol == Symbol.DIVIDE.getValue() || symbol == Symbol.PLUS.getValue()
                || symbol == Symbol.MINUS.getValue() || symbol == Symbol.MULTIPLY.getValue()
                || symbol == Symbol.GREATER_THAN.getValue() || symbol == Symbol.LESS_THAN.getValue()
                || symbol == Symbol.VERTICAL_BAR.getValue() || symbol == Symbol.TILDE.getValue()
                || symbol == Symbol.AND.getValue() || symbol == Symbol.EQUAL.getValue();
    }
    public int generateLabel(){
        return labelNumber++;
    }
}
