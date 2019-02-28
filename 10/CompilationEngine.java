import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {
    private Tokenizer tokenizer;
    private File outputFile;
    private TokenXmlBuilder tokenXmlBuilder = new TokenXmlBuilder();

    public CompilationEngine(Tokenizer tokenizer, File outputFile) {
        this.tokenizer = tokenizer;
        this.outputFile = outputFile;
    }

    public File writeOutputFile() throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
        bufferedWriter.write(tokenXmlBuilder.toString());
        bufferedWriter.flush();
        return outputFile;
    }

    public void compile() throws IOException {
        tokenizer.advance();
        TokenType tokenType = tokenizer.tokenType();
        if (tokenType == TokenType.KEYWORD) {
            Keyword keyword = tokenizer.keyword();
            if (keyword == Keyword.CLASS) {
                compileClass();
            } else {
                System.out.println("a class should start with keyword 'class'.");
            }
        }
    }

    public void compileStatements() throws IOException {
        // let if while do return
        tokenXmlBuilder.setStartNode("statements");
        // Keyword keyword = tokenizer.keyword();
        char symbol = tokenizer.symbol();
        while (symbol != Symbol.CLOSE_BRACE.getValue()) {
            Keyword keyword = tokenizer.keyword();
            if (keyword==null){ break;}
            if (Keyword.LET.value.equals(keyword.value)) {
                compileLet();
                tokenizer.advance();
            } else if (Keyword.WHILE.value.equals(keyword.value)) {
                compileWhile();
                tokenizer.advance();
            } else if (Keyword.IF.value.equals(keyword.value)) {
                compileIf();
            } else if (Keyword.DO.value.equals(keyword.value)){
                compileDo();
                tokenizer.advance();
            }else if (Keyword.RETURN.value.equals(keyword.value)){
                compileReturn();
            }
            symbol = tokenizer.symbol();
            //else if (keyword.value.equals(Keyword.))
        }
        tokenXmlBuilder.setEndNode("statements");

    }

    public void compileClass() throws IOException {
        // 'class' className '{' classVarDec* subroutineDec* '}'
        tokenXmlBuilder.setStartNode(Keyword.CLASS.value);
        // 'class'
        tokenXmlBuilder.addKeyword(Keyword.CLASS);
        //  className
        tokenizer.advance();
        TokenType identifierType = tokenizer.tokenType();
        String identifier = tokenizer.identifier();
        if (identifierType == TokenType.IDENTIFIER) {
            tokenXmlBuilder.addIdentifier(identifier);
        } else {
            System.out.println("class identifier did not appear");
            return;
        }
        // {
        tokenizer.advance();
        TokenType symbolType = tokenizer.tokenType();
        if (symbolType == TokenType.SYMBOL) {
            char symbol = tokenizer.symbol();
            tokenXmlBuilder.addSymbol(symbol);
        } else {
            System.out.println("miss symbol '{' after class identifier");
            return;
        }
        tokenizer.advance();
        //  classVarDec* subroutineDec*
        while (Symbol.CLOSE_BRACE.getValue()!=tokenizer.symbol()){
            TokenType tokenType = tokenizer.tokenType();
            if (TokenType.KEYWORD.equals(tokenType)){
                Keyword keyword = tokenizer.keyword();
                if (Keyword.FIELD.equals(keyword) || Keyword.STATIC.equals(keyword)){
                    compileClassVarDec(classLevelSymbolTable);
                }else if (Keyword.FUNCTION.equals(keyword)){
                    compileSubroutineDec(classLevelSymbolTable);
                }else if (Keyword.METHOD.equals(keyword)){
                    compileSubroutineDec(classLevelSymbolTable);
                }else if (Keyword.CONSTRUCTOR.equals(keyword)){
                    compileSubroutineDec(classLevelSymbolTable);
                }
            }
            tokenizer.advance();
        }
        // }
        tokenXmlBuilder.addSymbol(Symbol.CLOSE_BRACE.getValue());
        //</class>
        tokenXmlBuilder.setEndNode(Keyword.CLASS.value);
    }

    public void compileClassVarDec() throws IOException {
        // (static | field) type varName(,varName)* ';'
        tokenXmlBuilder.setStartNode("classVarDec");
        Keyword keyword = tokenizer.keyword();
        tokenXmlBuilder.addKeyword(keyword);
        tokenizer.advance();
        TokenType tokenType = tokenizer.tokenType();
        if (tokenType==TokenType.KEYWORD){
            String value = tokenizer.keyword().value;
            tokenXmlBuilder.addNodeAndAttribute(TokenType.KEYWORD.getValue(),value);
        }else if (TokenType.IDENTIFIER==tokenType){
            String identifier = tokenizer.identifier();
            tokenXmlBuilder.addNodeAndAttribute(TokenType.IDENTIFIER.getValue(),identifier);
        }
        tokenizer.advance();
        String identifier1 = tokenizer.identifier();
        tokenXmlBuilder.addNodeAndAttribute(TokenType.IDENTIFIER.getValue(),identifier1);
        tokenizer.advance();
        char symbol = tokenizer.symbol();
        while (symbol!=Symbol.SEMICOLON.getValue()){
            // ,
            tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(symbol));
            tokenizer.advance();
            String identifier = tokenizer.identifier();
            tokenXmlBuilder.addNodeAndAttribute(TokenType.IDENTIFIER.getValue(),identifier);
            tokenizer.advance();
            symbol = tokenizer.symbol();
        }
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(symbol));
        tokenXmlBuilder.setEndNode("classVarDec");

    }

    public void compileSubroutineDec() throws IOException {
        // (constructor|function|method) (void|type) subroutineName (parameterList) subroutineBody
        tokenXmlBuilder.setStartNode("subroutineDec");
        Keyword keyword = tokenizer.keyword();
        tokenXmlBuilder.addNodeAndAttribute(TokenType.KEYWORD.getValue(), keyword.value);
        tokenizer.advance();
        TokenType keywordOrIdentifierType = tokenizer.tokenType();
        if (keywordOrIdentifierType == TokenType.KEYWORD) {
            tokenXmlBuilder.addNodeAndAttribute(TokenType.KEYWORD.getValue(), tokenizer.keyword().value);
        } else if (keywordOrIdentifierType==TokenType.IDENTIFIER){
            tokenXmlBuilder.addNodeAndAttribute(TokenType.IDENTIFIER.getValue(), tokenizer.identifier());
        }
        tokenizer.advance();
        TokenType identifierType = tokenizer.tokenType();
        if (identifierType == TokenType.IDENTIFIER) {
            tokenXmlBuilder.addNodeAndAttribute(TokenType.IDENTIFIER.getValue(), tokenizer.identifier());
        } else {
            System.out.println("miss identifier after subroutine keyword");
        }
        tokenizer.advance();
        char symbol = tokenizer.symbol();
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(symbol));
        compileParameterList(subroutineSymbolTable);
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), ")");
        tokenizer.advance();
        compileSubroutineBody(subroutineSymbolTable);
        tokenXmlBuilder.setEndNode("subroutineDec");
    }

    public void compileParameterList() throws IOException {
        // (type varName (,type varName)*)?
        tokenXmlBuilder.setStartNode("parameterList");
        tokenizer.advance();
        while (tokenizer.symbol() != ')') {
            TokenType keywordType = tokenizer.tokenType();
            if (keywordType == TokenType.KEYWORD) {
                Keyword keyword = tokenizer.keyword();
                tokenXmlBuilder.addNodeAndAttribute(TokenType.KEYWORD.getValue(), keyword.value);
            } else {
                System.out.println("error miss parameter keyword");
            }
            tokenizer.advance();
            TokenType identifierType = tokenizer.tokenType();
            if (identifierType == TokenType.IDENTIFIER) {
                String identifier = tokenizer.identifier();
                tokenXmlBuilder.addNodeAndAttribute(TokenType.IDENTIFIER.getValue(), identifier);
            } else {
                System.out.println("error miss parameter identifier");
            }
            tokenizer.advance();
            TokenType commaOrEndParen = tokenizer.tokenType();
            if (commaOrEndParen == TokenType.SYMBOL) {
                char symbol = tokenizer.symbol();
                // ,
                if (symbol == Symbol.COMMA.getValue()) {
                    tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol));
                    tokenizer.advance();
                }
            }
        }
        tokenXmlBuilder.setEndNode("parameterList");
    }

    public void compileSubroutineBody() throws IOException {
        // '{' varDec* statements '}'
        tokenXmlBuilder.setStartNode("subroutineBody");
        TokenType symbolType = tokenizer.tokenType();
        if (symbolType == TokenType.SYMBOL) {
            char symbol = tokenizer.symbol();
            // {
            tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol));
        } else {
            System.out.println("miss { after parameterList");
        }
        tokenizer.advance();
        while (tokenizer.symbol() != Symbol.CLOSE_BRACE.getValue()) {
            TokenType tokenType = tokenizer.tokenType();
            if (tokenType == TokenType.KEYWORD) {
                Keyword keyword = tokenizer.keyword();
                if (keyword.value.equals(Keyword.VAR.value)) {
                    compileVarDec(subroutineSymbolTable);
                } else {
                    compileStatements();
                    tokenizer.advance();
                }
            }
        }
        // }
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(tokenizer.symbol()));
        tokenXmlBuilder.setEndNode("subroutineBody");
    }

    public void compileVarDec() throws IOException {
        // var type varName(',' varName)* ';'
        tokenXmlBuilder.setStartNode("varDec");
        // var
        tokenXmlBuilder.addNodeAndAttribute(TokenType.KEYWORD.getValue(), Keyword.VAR.value);
        tokenizer.advance();
        TokenType tokenType = tokenizer.tokenType();
        // var type
        if (tokenType == TokenType.IDENTIFIER) {
            String identifier = tokenizer.identifier();
            tokenXmlBuilder.addNodeAndAttribute(TokenType.IDENTIFIER.getValue(), identifier);
        } else if (tokenType == TokenType.KEYWORD) {
            Keyword keyword = tokenizer.keyword();
            tokenXmlBuilder.addNodeAndAttribute(TokenType.KEYWORD.getValue(), keyword.value);
        } else {
            System.out.println("miss identifier or keyword after var keyword");
        }
        // var int a,b,c,d;
        tokenizer.advance();
        while (tokenizer.symbol() != Symbol.SEMICOLON.getValue()) {
            TokenType identifierOrSymbol = tokenizer.tokenType();
            if (identifierOrSymbol == TokenType.IDENTIFIER) {
                String identifier2 = tokenizer.identifier();
                tokenXmlBuilder.addNodeAndAttribute(TokenType.IDENTIFIER.getValue(), identifier2);
            } else if (identifierOrSymbol == TokenType.SYMBOL) {
                char symbol = tokenizer.symbol();
                if (symbol == Symbol.COMMA.getValue()) {
                    tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol));
                }
            }
            tokenizer.advance();
        }
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(Symbol.SEMICOLON.getValue()));
        tokenXmlBuilder.setEndNode("varDec");
        tokenizer.advance();
    }

    public void compileLet() throws IOException {
        // 'let' varName ('[' expression ']')? '=' expression ';'
        tokenXmlBuilder.setStartNode("letStatement");
        // let
        tokenXmlBuilder.addNodeAndAttribute(TokenType.KEYWORD.getValue(), Keyword.LET.value);
        tokenizer.advance();
        String identifier1 = tokenizer.identifier();
        // varName
        tokenXmlBuilder.addNodeAndAttribute(TokenType.IDENTIFIER.getValue(),identifier1);
        tokenizer.advance();
        char symbol1 = tokenizer.symbol();
        if (symbol1 == Symbol.EQUAL.getValue()) {
            // =
            tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol1));
            tokenizer.advance();
        }else if (symbol1==Symbol.OPEN_BRACKET.getValue()){
            // [
            tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol1));
            tokenizer.advance();
            // expression
            compileExpression();
            // ]
            tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(Symbol.CLOSE_BRACKET.getValue()));
            tokenizer.advance();
            // =
            tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(tokenizer.symbol()));
            tokenizer.advance();
        }
        // expression
        compileExpression();
        System.out.println(tokenizer.symbol());
        // ;
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(Symbol.SEMICOLON.getValue()));
        tokenXmlBuilder.setEndNode("letStatement");
    }

    public void compileIf() throws IOException {
        // 'if' '(' expression ')' '{' statements '}' ('else' '{' statements '}')?
        tokenXmlBuilder.setStartNode("ifStatement");
        // if
        tokenXmlBuilder.addNodeAndAttribute(TokenType.KEYWORD.getValue(), Keyword.IF.value);
        // if (
        tokenizer.advance();
        char symbol = tokenizer.symbol();
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(symbol));
        // if( expression
        tokenizer.advance();
        compileExpression();
        // if(expression)
        char symbol1 = tokenizer.symbol();
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(symbol1));
        tokenizer.advance();
        // if(expression) {
        char symbol2 = tokenizer.symbol();
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(symbol2));
        tokenizer.advance();
        // if(expression){ statements
        compileStatements();
        // tokenizer.advance();
        // if(expression){statements }
        char symbol3 = tokenizer.symbol();
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(symbol3));
        tokenizer.advance();
        Keyword keyword = tokenizer.keyword();
        if (keyword==Keyword.ELSE){
            tokenXmlBuilder.addNodeAndAttribute(TokenType.KEYWORD.getValue(),Keyword.ELSE.value);
            tokenizer.advance();
            tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(tokenizer.symbol()));
            tokenizer.advance();
            compileStatements();
            tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(Symbol.CLOSE_BRACE.getValue()));
            tokenizer.advance();
        }else {
            //tokenizer.moveBack();
        }
        tokenXmlBuilder.setEndNode("ifStatement");
    }

    public void compileWhile() throws IOException {
        System.out.println("while");
        tokenXmlBuilder.setStartNode("whileStatement");
        // while
        tokenXmlBuilder.addNodeAndAttribute(TokenType.KEYWORD.getValue(), Keyword.WHILE.value);
        tokenizer.advance();
        TokenType tokenType = tokenizer.tokenType();
        if (tokenType == TokenType.SYMBOL) {
            // while (
            char symbol = tokenizer.symbol();
            tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol));
        }
        tokenizer.advance();
        compileExpression();
        char symbol = tokenizer.symbol();
        //while(expression )
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol));
        tokenizer.advance();
        char symbol1 = tokenizer.symbol();
        // while(expression) {
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol1));
        // while(expression){ statements
        tokenizer.advance();
        compileStatements();
        // while(expression){statements}
        char symbol3 = tokenizer.symbol();
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(symbol3));
        tokenXmlBuilder.setEndNode("whileStatement");
    }

    public void compileDo() throws IOException {
        System.out.println("do");
        // 'do' subroutineCall ';'
        // subroutineCall subroutineName '(' expressionList ')'
        //              | (classname|varName) '.' subroutineName '('expressionList')'
        tokenXmlBuilder.setStartNode("doStatement");
        // do
        tokenXmlBuilder.addNodeAndAttribute(TokenType.KEYWORD.getValue(),Keyword.DO.value);
        // do a
        tokenizer.advance();
        String identifier = tokenizer.identifier();
        tokenXmlBuilder.addNodeAndAttribute(TokenType.IDENTIFIER.getValue(),identifier);
        tokenizer.advance();
        char symbol = tokenizer.symbol();
        if (symbol==Symbol.PERIOD.getValue()){
            // do a.
            tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(symbol));
            tokenizer.advance();
            // do a.b
            String identifier1 = tokenizer.identifier();
            tokenXmlBuilder.addNodeAndAttribute(TokenType.IDENTIFIER.getValue(),identifier1);
            tokenizer.advance();
        }
        // do a.b( || do a(
        char symbol1 = tokenizer.symbol();
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(symbol1));
        // do a.b(expressionList
        compileExpressionList();
        // do a.b(expressionList)
        char symbol2 = tokenizer.symbol();
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(symbol2));
        tokenizer.advance();
        // do a.b(expressionList) ;
        char symbol3 = tokenizer.symbol();
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(symbol3));
        tokenXmlBuilder.setEndNode("doStatement");
    }

    public void compileReturn() throws IOException {
        // return expression? ;
        tokenXmlBuilder.setStartNode("returnStatement");
        tokenXmlBuilder.addNodeAndAttribute(TokenType.KEYWORD.getValue(),Keyword.RETURN.value);
        tokenizer.advance();
        char symbol = tokenizer.symbol();
        while (tokenizer.symbol() != Symbol.SEMICOLON.getValue()){
            compileExpression();
            symbol = tokenizer.symbol();
        }
        tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(),String.valueOf(symbol));
        tokenXmlBuilder.setEndNode("returnStatement");
    }

    public void compileExpression() throws IOException {
        // term(op term)*
        tokenXmlBuilder.setStartNode("expression");
        // term
        compileTerm();
        // op
        while (isOperationSymbol(tokenizer.symbol())) {
            tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(tokenizer.symbol()));
            tokenizer.advance();
            // term
            compileTerm();
        }
        tokenXmlBuilder.setEndNode("expression");
    }

    public void compileTerm() throws IOException {
        // integerConstant | stringConstant | keywordConstant |
        // varName | varName[expression] | subroutineCall | (expression) | unArrayOp term
        tokenXmlBuilder.setStartNode("term");
        TokenType tokenType = tokenizer.tokenType();
        if (tokenType == TokenType.INT_CONST) {
            // integerConstant
            int i = tokenizer.intVal();
            tokenXmlBuilder.addNodeAndAttribute(TokenType.INT_CONST.getValue(), String.valueOf(i));
            tokenizer.advance();
        }else if (tokenType == TokenType.STRING_CONST) {
            // stringConstant
            String stringVal = tokenizer.stringVal();
            tokenXmlBuilder.addNodeAndAttribute(TokenType.STRING_CONST.getValue(), stringVal);
            tokenizer.advance();
        } else if (tokenType == TokenType.KEYWORD){
            // keywordConstant
            String value = tokenizer.keyword().value;
            tokenXmlBuilder.addNodeAndAttribute(TokenType.KEYWORD.getValue(),value);
            tokenizer.advance();
        }else if (tokenType == TokenType.IDENTIFIER) {
            // varName
            String identifier = tokenizer.identifier();
            tokenXmlBuilder.addNodeAndAttribute(TokenType.IDENTIFIER.getValue(), identifier);
            tokenizer.advance();
            TokenType nextType = tokenizer.tokenType();
            if (nextType == TokenType.SYMBOL) {
                // op or [ or ( or .
                char symbol = tokenizer.symbol();
                // subroutineCall
                // subroutineName '(' expressionList ')'
                // (className|varName) '.' subroutineName '(' expressionList ')'
                if (Symbol.PERIOD.getValue() == symbol) {
                    tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(Symbol.PERIOD.getValue()));
                    tokenizer.advance();
                    TokenType subIdentifier = tokenizer.tokenType();
                    if (subIdentifier == TokenType.IDENTIFIER) {
                        // a.b
                        String subIdentifierName = tokenizer.identifier();
                        tokenXmlBuilder.addNodeAndAttribute(TokenType.IDENTIFIER.getValue(), subIdentifierName);
                        tokenizer.advance();
                        char symbol1 = tokenizer.symbol();
                        if (symbol1 == Symbol.OPEN_PAREN.getValue()) {
                            // a.b(
                             tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol1));
                            // expressionList
                            compileExpressionList();
                             // tokenizer.advance();
                             char symbol2 = tokenizer.symbol();
                            //a.b( expressionList )
                            tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol2));
                            tokenizer.advance();
                        }
                    }
                } else if (Symbol.OPEN_PAREN.getValue() == symbol) {
                    // a(
                    tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol));
                    // a(expressionList
                    compileExpressionList();
                    // a(expressionList)
                    char symbol1 = tokenizer.symbol();
                    tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol1));
                }else if (Symbol.OPEN_BRACKET.getValue() == symbol) {
                    // a[
                    tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol));
                    // a[expression
                    tokenizer.advance();
                    compileExpression();
                    // a[expression]
                    char symbol1 = tokenizer.symbol();
                    tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol1));
                    tokenizer.advance();
                }
            }
        } else if (tokenType == TokenType.SYMBOL) {
            char symbol = tokenizer.symbol();
            if (symbol == Symbol.OPEN_PAREN.getValue()) {
                tokenXmlBuilder.addSymbol(symbol);
                tokenizer.advance();
                compileExpression();
                tokenXmlBuilder.addSymbol(tokenizer.symbol());
                tokenizer.advance();
            }else if(symbol ==Symbol.TILDE.getValue()||symbol==Symbol.MINUS.getValue()){
                // unArrayOp - ~
                tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol));
                tokenizer.advance();
                // term
                compileTerm();
            }else {
                tokenXmlBuilder.addNodeAndAttribute(TokenType.SYMBOL.getValue(), String.valueOf(symbol));
            }
        }
        tokenXmlBuilder.setEndNode("term");
    }

    public void compileExpressionList() throws IOException {
        // (expression(, expression)*)?
        tokenXmlBuilder.setStartNode("expressionList");
        tokenizer.advance();
        while (Symbol.CLOSE_PAREN.getValue() !=  tokenizer.symbol()){
            if ( tokenizer.symbol() == Symbol.COMMA.getValue()){
                tokenXmlBuilder.addSymbol( tokenizer.symbol());
                tokenizer.advance();
                compileExpression();
            }else {
                compileExpression();
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
