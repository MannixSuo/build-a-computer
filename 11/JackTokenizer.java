import java.io.*;

public class JackTokenizer {

    private BufferedReader bufferedReader;
    private int maxCacheSize = 20;
    // circle byte
    byte[] cache = new byte[maxCacheSize];
    private int start = 0,end = 0;
    private String currentToken;
    private boolean moveBack = false;
    private File cacheFile;

    private int read1() throws IOException {
        int read = bufferedReader.read();
        return read;
    }

    private int newStart(){
        if (start==20){
            start = 0;
        } else {
            start = start + 1;
        }
        return start;
    }

    public JackTokenizer(File file) {
        try {
            cacheFile = new File(file.getPath() + "cache");
            FileWriter fileWriter = new FileWriter(cacheFile);
            this.bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line=bufferedReader.readLine())!=null){
                if (line.startsWith("//")){
                    continue;
                }else if (line.contains("//")){
                    line = line.substring(0, line.lastIndexOf("//"));
                }else if (line.contains("/**")){
                    if (line.contains("*/")){
                        continue;
                    }else {
                        while (true){
                            line = bufferedReader.readLine();
                            if (line.contains("*/")){
                                line = bufferedReader.readLine();
                                break;
                            }
                        }
                    }
                } if (line.length()==0){
                    continue;
                }else {
                    fileWriter.write(line + "\n");
                }
            }
            fileWriter.close();
            this.bufferedReader = new BufferedReader(new FileReader(cacheFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * read next character
     * @return -1 for end
     * @throws IOException  if reader is null
     */
    private int read() throws IOException {
        if (bufferedReader!=null){
            return bufferedReader.read();
        } else{
            throw new IOException();
        }
    }

    /**
     * whether this file contain more tokens
     * after this method the reader's current
     * index is the white space before next tokens
     *
     * @return boolean
     */
    public boolean hasMoreTokens() throws IOException {
        bufferedReader.mark(1);
        int read = bufferedReader.read();
        bufferedReader.reset();
        if (read==-1){
            bufferedReader.close();
        }
        return read!=-1;
    }


private int line = 1;
    public void advance() throws IOException {
        // if white space stop read
        // if special symbol stop read
        //
        if (moveBack){
            moveBack = false;
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        boolean singleSymbol = true;
        boolean start = true;
        boolean isComment = false;
        boolean stringEnd = false;
        bufferedReader.mark(100);
        while (true){
            int read = bufferedReader.read();
            if (read==-1){
                return;
            }
            if (read == 47){
                bufferedReader.mark(1);
                int  read1 = bufferedReader.read();
                if (read1 == 47 || read1 == 42 ){
                    isComment = true;
                }else {
                    bufferedReader.reset();
                }
            }else {
                isComment =false;
            }
            if (isComment){
                while (true){
                    if (isNewLine(bufferedReader.read())){
                        break;
                    }
                }
            }
            if ( read == 34){
                stringBuilder.append((char) read);
                while (true){
                    int read1 = bufferedReader.read();
                    stringBuilder.append((char) read1);
                    if (read1 == 34){
                        stringEnd = true;
                        break;
                    }
                }
            }
            if (isWhiteSpace(read)){
                if (start){
                    continue;
                }
                break;
            }
            if (stringEnd){
                bufferedReader.mark(10);
                break;
            }
            if (!isSpecialSymbol(read)){
                bufferedReader.mark(10);
                singleSymbol = false;
                stringBuilder.append((char)read);
            }
            if (isSpecialSymbol(read) && !singleSymbol){
                bufferedReader.reset();
                break;
            }
            if (isSpecialSymbol(read) && singleSymbol && !isComment){
                stringBuilder.append((char)read);
                break;
            }

            start = false;
        }
        if (stringBuilder.length() == 0){
            advance();
        }else {
            currentToken = stringBuilder.toString();
        }
    }
    public void moveBack(){
        this.moveBack = true;
    }

    private boolean isSpecialSymbol(int value) {
        // 0-9
        boolean isNumber = value>47&&value<58;
        // A-Z
        boolean isA_Z = value>64&&value<91;
        // a-z
        boolean isa_z = value>96&&value<123;
        // _
        boolean is_ = value == 95;
        if (isNumber || isA_Z || isa_z || is_){
            return false;
        }
        if (value == 34){
            return false;
        }
        return true;
    }

    public String currentString(){
        if (currentToken.startsWith("\"")&&currentToken.endsWith("\"")){
            currentToken = currentToken.substring(1,currentToken.length()-1);
        }
        return currentToken;
    }

    public TokenType tokenType(){
        if (Keyword.contains(currentToken)){
            return TokenType.KEYWORD;
        }
        if (Symbol.contain(currentToken.charAt(0))){
            return TokenType.SYMBOL;
        }
        if (currentToken.startsWith("\"")&&currentToken.endsWith("\"")){
            return TokenType.STRING_CONST;
        }
        if (currentToken.matches("\\d+")){
            return TokenType.INT_CONST;
        }
        if (currentToken.matches("[a-zA-Z0-9_]+")){
            return TokenType.IDENTIFIER;
        }
        return null;
    }

    public Keyword keyword(){
       return Keyword.getKeyword(currentToken);
    }

    public char symbol(){
        return Symbol.getSymbol(currentToken.charAt(0));
    }

    public String identifier(){
        return currentToken;
    }

    public int intVal(){
        return Integer.valueOf(currentToken);
    }

    public String stringVal(){
        return currentToken.substring(1,currentToken.length()-1);
    }

    private boolean isWhiteSpace(int value){
        // \n
        if (value == 10){
            return true;
        }
        // \r
        if (value == 13){
            return true;
        }
        // space
        if (value == 32){
            return true;
        }
        // \t
        if (value == 9){
            return true;
        }
        return false;
    }

    private boolean isNewLine(int value){
        // \n
        if (value == 10){
            line++;
            return true;
        }
        // \r
        if (value == 13){
            line++;
            return true;
        }
        return false;
    }
    public void removeCacheFile() throws IOException {
        bufferedReader.close();
        boolean delete = cacheFile.delete();
    }
}
