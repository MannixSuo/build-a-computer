import java.io.File;
import java.io.IOException;

public class CompilationEngine {

    private JackTokenizer jackTokenizer;
    private TokenXmlBuilder tokenXmlBuilder = new TokenXmlBuilder();
    private VMWriter writer;
    private String className = "noClass";

    public CompilationEngine(JackTokenizer jackTokenizer, File outputFile) throws IOException {
        this.jackTokenizer = jackTokenizer;
        this.writer = new VMWriter(outputFile);
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
        // let if while do return
        char symbol = jackTokenizer.symbol();
        while (symbol != Symbol.CLOSE_BRACE.getValue()) {
            Keyword keyword = jackTokenizer.keyword();
            if (keyword==null){ break;}
            if (Keyword.LET.value.equals(keyword.value)) {
                compileLet(subroutineSymbolTable);
                jackTokenizer.advance();
            } else if (Keyword.WHILE.value.equals(keyword.value)) {
                compileWhile(subroutineSymbolTable);
                jackTokenizer.advance();
            } else if (Keyword.IF.value.equals(keyword.value)) {
                compileIf(subroutineSymbolTable);
            } else if (Keyword.DO.value.equals(keyword.value)){
                compileDo(subroutineSymbolTable);
                jackTokenizer.advance();
            }else if (Keyword.RETURN.value.equals(keyword.value)){
                compileReturn(subroutineSymbolTable);
            }
            symbol = jackTokenizer.symbol();
            //else if (keyword.value.equals(Keyword.))
        }
        tokenXmlBuilder.setEndNode("statements");

    }

    public void compileClass() throws IOException {
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
        while (Symbol.CLOSE_BRACE.getValue() != jackTokenizer.symbol()){
            TokenType tokenType = jackTokenizer.tokenType();
            if (TokenType.KEYWORD.equals(tokenType)){
                Keyword keyword = jackTokenizer.keyword();
                if (Keyword.FIELD.equals(keyword) || Keyword.STATIC.equals(keyword)){
                    compileClassVarDec(classLevelSymbolTable);
                }else if (Keyword.FUNCTION.equals(keyword)){
                    compileSubroutineDec(classLevelSymbolTable);
                }else if (Keyword.METHOD.equals(keyword)){
                    compileSubroutineDec(classLevelSymbolTable);
                }else if (Keyword.CONSTRUCTOR.equals(keyword)){
                    compileSubroutineDec(classLevelSymbolTable);
                }else {
                    System.out.println("unknown code : " + keyword.value);
                    return;
                }
            }
            jackTokenizer.advance();
        }
        // }
        tokenXmlBuilder.addSymbol(Symbol.CLOSE_BRACE.getValue());
        //</class>
        tokenXmlBuilder.setEndNode(Keyword.CLASS.value);
    }

    public void compileClassVarDec(SymbolTable classLevelSymbolTable) throws IOException {
        // (static | field) type varName(,varName)* ';'
        Keyword varScopeKeyword = jackTokenizer.keyword();
        // (static | field)
        String kind = varScopeKeyword.value;
        jackTokenizer.advance();
        TokenType tokenType = jackTokenizer.tokenType();
        // type
        String varType = "";
        if (tokenType==TokenType.KEYWORD){
            varType = jackTokenizer.keyword().value;
        }else if (TokenType.IDENTIFIER==tokenType){
            varType = jackTokenizer.identifier();
        }
        jackTokenizer.advance();
        // varName
        String varName = jackTokenizer.identifier();

        classLevelSymbolTable.addClassLevelSymbol(varName,varType,kind);
        jackTokenizer.advance();
        char symbol = jackTokenizer.symbol();
        while (symbol!=Symbol.SEMICOLON.getValue()){
            // ,
            jackTokenizer.advance();
            // varName
            varName = jackTokenizer.identifier();

            classLevelSymbolTable.addClassLevelSymbol(varName,varType,kind);

            jackTokenizer.advance();
            symbol = jackTokenizer.symbol();
        }
    }

    public void compileSubroutineDec(SymbolTable classLevelSymbolTable) throws IOException {
        // (constructor|function|method) (void|type) subroutineName (parameterList) subroutineBody
        SymbolTable subroutineSymbolTable = classLevelSymbolTable.startSubroutine();
        // (constructor|function|method)
        Keyword keyword = jackTokenizer.keyword();

        if (keyword.is(Keyword.METHOD)){
            subroutineSymbolTable.addSubroutineLevelSymbol("this",className,Node.KIND_ARGUMENT);
        }
        jackTokenizer.advance();

        // (void|type)
        TokenType keywordOrIdentifierType = jackTokenizer.tokenType();

        if (keywordOrIdentifierType == TokenType.KEYWORD) {
        } else if (keywordOrIdentifierType==TokenType.IDENTIFIER){
        }

        jackTokenizer.advance();
        // subroutineName
        TokenType identifierType = jackTokenizer.tokenType();
        if (identifierType == TokenType.IDENTIFIER) {
        } else {
            System.out.println("miss identifier after subroutine keyword");
        }
        jackTokenizer.advance();
        // (
        char symbol = jackTokenizer.symbol();
        compileParameterList(subroutineSymbolTable);
        // )
        jackTokenizer.advance();
        compileSubroutineBody(subroutineSymbolTable);
        tokenXmlBuilder.setEndNode("subroutineDec");
    }

    public void compileParameterList(SymbolTable subroutineSymbolTable) throws IOException {
        // (type varName (,type varName)*)?
        jackTokenizer.advance();
        while (jackTokenizer.symbol() != ')') {
            // type
            TokenType keywordOrIdentifier = jackTokenizer.tokenType();
            String type ;
            if (keywordOrIdentifier == TokenType.KEYWORD) {
                Keyword keyword = jackTokenizer.keyword();
                type = keyword.value;
            }else if(keywordOrIdentifier == TokenType.IDENTIFIER) {
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
                subroutineSymbolTable.addSubroutineLevelSymbol(identifier,type,Node.KIND_ARGUMENT);
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
        }
    }

    public void compileSubroutineBody(SymbolTable subroutineSymbolTable) throws IOException {
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
                    compileStatements(subroutineSymbolTable);
                    jackTokenizer.advance();
                }
            }
        }
        // }
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(jackTokenizer.symbol()));
        tokenXmlBuilder.setEndNode("subroutineBody");
    }

    public void compileVarDec(SymbolTable subroutineSymbolTable) throws IOException {
        // var type varName(',' varName)* ';'
        // 'var'
        jackTokenizer.advance();
        TokenType tokenType = jackTokenizer.tokenType();
        // type
        String type;
        if (tokenType == TokenType.IDENTIFIER) {
            type  = jackTokenizer.identifier();
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
                subroutineSymbolTable.addSubroutineLevelSymbol(identifier2,type,Node.KIND_LOCAL);
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
        // 'let' varName ('[' expression ']')? '=' expression ';'
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
        }else if (symbol1==Symbol.OPEN_BRACKET.getValue()){
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

        // pop  kind index
        writer.writePopSymbol(symbol);
        System.out.println(jackTokenizer.symbol());
        // ;
    }

    public void compileIf(SymbolTable subroutineSymbolTable) throws IOException {
        // 'if' '(' expression ')' '{' statements '}' ('else' '{' statements '}')?
        tokenXmlBuilder.setStartNode("ifStatement");
        // if
        tokenXmlBuilder.addNodeAndAttribute(TokenType.KEYWORD.getValue(), Keyword.IF.value);
        // if (
        jackTokenizer.advance();
        char symbol = jackTokenizer.symbol();
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(symbol));
        // if( expression
        jackTokenizer.advance();
        compileExpression(subroutineSymbolTable);
        // if(expression)
        char symbol1 = jackTokenizer.symbol();
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(symbol1));
        jackTokenizer.advance();
        // if(expression) {
        char symbol2 = jackTokenizer.symbol();
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(symbol2));
        jackTokenizer.advance();
        // if(expression){ statements
        compileStatements(subroutineSymbolTable);
        // jackTokenizer.advance();
        // if(expression){statements }
        char symbol3 = jackTokenizer.symbol();
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(symbol3));
        jackTokenizer.advance();
        Keyword keyword = jackTokenizer.keyword();
        if (keyword==Keyword.ELSE){
            tokenXmlBuilder.addNodeAndAttribute(TokenType.KEYWORD.getValue(),Keyword.ELSE.value);
            jackTokenizer.advance();
            tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(jackTokenizer.symbol()));
            jackTokenizer.advance();
            compileStatements(subroutineSymbolTable);
            tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(Symbol.CLOSE_BRACE.getValue()));
            jackTokenizer.advance();
        }else {
            //jackTokenizer.moveBack();
        }
        tokenXmlBuilder.setEndNode("ifStatement");
    }

    public void compileWhile(SymbolTable subroutineSymbolTable) throws IOException {
        System.out.println("while");
        tokenXmlBuilder.setStartNode("whileStatement");
        // while
        tokenXmlBuilder.addNodeAndAttribute(TokenType.KEYWORD.getValue(), Keyword.WHILE.value);
        jackTokenizer.advance();
        TokenType tokenType = jackTokenizer.tokenType();
        if (tokenType == TokenType.SYMBOL) {
            // while (
            char symbol = jackTokenizer.symbol();
            tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol));
        }
        jackTokenizer.advance();
        compileExpression(subroutineSymbolTable);
        char symbol = jackTokenizer.symbol();
        //while(expression )
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol));
        jackTokenizer.advance();
        char symbol1 = jackTokenizer.symbol();
        // while(expression) {
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol1));
        // while(expression){ statements
        jackTokenizer.advance();
        compileStatements(subroutineSymbolTable);
        // while(expression){statements}
        char symbol3 = jackTokenizer.symbol();
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(symbol3));
        tokenXmlBuilder.setEndNode("whileStatement");
    }

    public void compileDo(SymbolTable subroutineSymbolTable) throws IOException {
        System.out.println("do");
        // 'do' subroutineCall ';'
        // subroutineCall subroutineName '(' expressionList ')'
        //              | (classname|varName) '.' subroutineName '('expressionList')'
        tokenXmlBuilder.setStartNode("doStatement");
        // do
        tokenXmlBuilder.addNodeAndAttribute(TokenType.KEYWORD.getValue(),Keyword.DO.value);
        // do a
        jackTokenizer.advance();
        String identifier = jackTokenizer.identifier();
        tokenXmlBuilder.addNodeAndAttribute(TokenType.IDENTIFIER.getValue(),identifier);
        jackTokenizer.advance();
        char symbol = jackTokenizer.symbol();
        if (symbol==Symbol.PERIOD.getValue()){
            // do a.
            tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(symbol));
            jackTokenizer.advance();
            // do a.b
            String identifier1 = jackTokenizer.identifier();
            tokenXmlBuilder.addNodeAndAttribute(TokenType.IDENTIFIER.getValue(),identifier1);
            jackTokenizer.advance();
        }
        // do a.b( || do a(
        char symbol1 = jackTokenizer.symbol();
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(symbol1));
        // do a.b(expressionList
        compileExpressionList(subroutineSymbolTable);
        // do a.b(expressionList)
        char symbol2 = jackTokenizer.symbol();
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(symbol2));
        jackTokenizer.advance();
        // do a.b(expressionList) ;
        char symbol3 = jackTokenizer.symbol();
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(symbol3));
        tokenXmlBuilder.setEndNode("doStatement");
    }

    public void compileReturn(SymbolTable subroutineSymbolTable) throws IOException {
        // return expression? ;
        tokenXmlBuilder.setStartNode("returnStatement");
        tokenXmlBuilder.addNodeAndAttribute(TokenType.KEYWORD.getValue(),Keyword.RETURN.value);
        jackTokenizer.advance();
        char symbol = jackTokenizer.symbol();
        while (jackTokenizer.symbol() != Symbol.SEMICOLON.getValue()){
            compileExpression(subroutineSymbolTable);
            symbol = jackTokenizer.symbol();
        }
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(symbol));
        tokenXmlBuilder.setEndNode("returnStatement");
    }

    public void compileExpression(SymbolTable subroutineSymbolTable) throws IOException {
        // term(op term)*
        // term
        compileTerm(subroutineSymbolTable);
        // op
        while (isOperationSymbol(jackTokenizer.symbol())) {
            // op
            char op = jackTokenizer.symbol();
            jackTokenizer.advance();
            // term
            compileTerm(subroutineSymbolTable);
            writer.writeArithmetic(op);
        }
        tokenXmlBuilder.setEndNode("expression");
    }

    public void compileTerm(SymbolTable subroutineSymbolTable) throws IOException {
        // integerConstant | stringConstant | keywordConstant |
        // varName | varName[expression] | subroutineCall | (expression) | unArrayOp term
        TokenType tokenType = jackTokenizer.tokenType();
        if (tokenType == TokenType.INT_CONST) {
            // integerConstant
            int i = jackTokenizer.intVal();
            jackTokenizer.advance();
            writer.writePushConst(i);
        }else if (tokenType == TokenType.STRING_CONST) {
            // stringConstant
            String stringVal = jackTokenizer.stringVal();
            tokenXmlBuilder.addNodeAndAttribute(TokenType.STRING_CONST.getValue(), stringVal);
            jackTokenizer.advance();
        } else if (tokenType == TokenType.KEYWORD){
            // keywordConstant
            String value = jackTokenizer.keyword().value;
            tokenXmlBuilder.addNodeAndAttribute(TokenType.KEYWORD.getValue(),value);
            jackTokenizer.advance();
        }else if (tokenType == TokenType.IDENTIFIER) {
            // varName
            String identifier = jackTokenizer.identifier();
            tokenXmlBuilder.addNodeAndAttribute(TokenType.IDENTIFIER.getValue(), identifier);
            jackTokenizer.advance();
            TokenType nextType = jackTokenizer.tokenType();
            if (nextType == TokenType.SYMBOL) {
                // op or [ or ( or .
                char symbol = jackTokenizer.symbol();
                // subroutineCall
                // subroutineName '(' expressionList ')'
                // (className|varName) '.' subroutineName '(' expressionList ')'
                if (Symbol.PERIOD.getValue() == symbol) {
                    tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(Symbol.PERIOD.getValue()));
                    jackTokenizer.advance();
                    TokenType subIdentifier = jackTokenizer.tokenType();
                    if (subIdentifier == TokenType.IDENTIFIER) {
                        // a.b
                        String subIdentifierName = jackTokenizer.identifier();
                        tokenXmlBuilder.addNodeAndAttribute(TokenType.IDENTIFIER.getValue(), subIdentifierName);
                        jackTokenizer.advance();
                        char symbol1 = jackTokenizer.symbol();
                        if (symbol1 == Symbol.OPEN_PAREN.getValue()) {
                            // a.b(
                             tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol1));
                            // expressionList
                            compileExpressionList(subroutineSymbolTable);
                             // jackTokenizer.advance();
                             char symbol2 = jackTokenizer.symbol();
                            //a.b( expressionList )
                            tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol2));
                            jackTokenizer.advance();
                        }
                    }
                } else if (Symbol.OPEN_PAREN.getValue() == symbol) {
                    // a(
                    tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol));
                    // a(expressionList
                    compileExpressionList(subroutineSymbolTable);
                    // a(expressionList)
                    char symbol1 = jackTokenizer.symbol();
                    tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol1));
                }else if (Symbol.OPEN_BRACKET.getValue() == symbol) {
                    // a[
                    tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol));
                    // a[expression
                    jackTokenizer.advance();
                    compileExpression(subroutineSymbolTable);
                    // a[expression]
                    char symbol1 = jackTokenizer.symbol();
                    tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol1));
                    jackTokenizer.advance();
                }
            }
        } else if (tokenType == TokenType.SYMBOL) {
            char symbol = jackTokenizer.symbol();
            if (symbol == Symbol.OPEN_PAREN.getValue()) {
                tokenXmlBuilder.addSymbol(symbol);
                jackTokenizer.advance();
                compileExpression(subroutineSymbolTable);
                tokenXmlBuilder.addSymbol(jackTokenizer.symbol());
                jackTokenizer.advance();
            }else if(symbol ==Symbol.TILDE.getValue()||symbol==Symbol.MINUS.getValue()){
                // unArrayOp - ~
                tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol));
                jackTokenizer.advance();
                // term
                compileTerm(subroutineSymbolTable);
            }else {
                tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol));
            }
        }
        tokenXmlBuilder.setEndNode("term");
    }

    public void compileExpressionList(SymbolTable subroutineSymbolTable) throws IOException {
        // (expression(, expression)*)?
        tokenXmlBuilder.setStartNode("expressionList");
        jackTokenizer.advance();
        while (Symbol.CLOSE_PAREN.getValue() !=  jackTokenizer.symbol()){
            if ( jackTokenizer.symbol() == Symbol.COMMA.getValue()){
                tokenXmlBuilder.addSymbol( jackTokenizer.symbol());
                jackTokenizer.advance();
                compileExpression(subroutineSymbolTable);
            }else {
                compileExpression(subroutineSymbolTable);
            }
        }
        tokenXmlBuilder.setEndNode("expressionList");
    }

    private boolean isOperationSymbol(char symbol) {
        // + - * / return true
        return symbol == Symbol.DIVIDE.getValue() || symbol == Symbol.PLUS.getValue()
                || symbol == Symbol.MINUS.getValue() || symbol == Symbol.MULTIPLY.getValue()
                || symbol == Symbol.GREATER_THAN.getValue() || symbol == Symbol.LESS_THAN.getValue()
                || symbol == Symbol.VERTICAL_BAR.getValue() || symbol == Symbol.TILDE.getValue()
                || symbol == Symbol.AND.getValue() || symbol==Symbol.EQUAL.getValue();
    }
}
