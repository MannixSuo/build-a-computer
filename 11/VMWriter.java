import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
    private BufferedWriter outputFileWriter;

    public VMWriter(File outputFile) throws IOException {
        this.outputFileWriter = new BufferedWriter(new FileWriter(outputFile));
    }

    private void writePush(String segments, int index) throws IOException {
        outputFileWriter.write(String.format("push %s %d %n", segments, index));
    }


    private void writePop(String segments, int index) throws IOException {
        outputFileWriter.write(String.format("pop %s %d %n", segments, index));
    }

    public void writeArithmetic(char command) throws IOException {
        if ('+' == command){
            outputFileWriter.write(String.format("add %n"));
        }else if ('-'==command){
            outputFileWriter.write(String.format("sub %n"));
        }else if ('*'==command){
            writeCall("Math.multiply",2);
        }else {
            writeCall("Math.divide",2);
        }
    }
    public void writeLogic(char command) throws IOException {
        if ('~'==command){
            outputFileWriter.write(String.format("not %n"));
        }else if ('-'==command){
            outputFileWriter.write(String.format("neg %n"));
        }else if ('>'==command){
            outputFileWriter.write(String.format("gt %n"));
        }else if ('<'==command){
            outputFileWriter.write(String.format("lt %n"));
        }else if ('=' == command){
            outputFileWriter.write(String.format("eq %n"));
        }else if ('&'==command){
            outputFileWriter.write(String.format("and %n"));
        }else if ('|'==command){
            outputFileWriter.write(String.format("or %n"));
        }else {
            System.out.println("unknown logic symbol: "+command);
        }
    }

    public void writeLabel(String label) throws IOException {
        outputFileWriter.write(String.format("label %s %n", label));
    }

    public void writeGoto(String label) throws IOException {
        outputFileWriter.write(String.format("goto %s %n", label));
    }

    public void writeIf(String label) throws IOException {
        outputFileWriter.write(String.format("if-goto %s %n", label));
    }

    public void writeCall(String name, int nArgs) throws IOException {
        outputFileWriter.write(String.format("call %s %d %n", name, nArgs));
    }

    public void writeFunction(String name,int nLocals) throws IOException {
        outputFileWriter.write(String.format("function %s %d %n",name,nLocals));
    }
    public void writeReturn() throws IOException {
        outputFileWriter.write("return %n");
    }

    public void writePushSymbol(Node symbol) throws IOException {
        if (Node.KIND_FIELD.equals(symbol.getKind())){
            writePush("this",symbol.getIndex());
        }else {
            writePush(symbol.getKind(),symbol.getIndex());
        }
    }

    public void writePopSymbol(Node symbol) throws IOException {
        if (Node.KIND_FIELD.equals(symbol.getKind())){
            writePop("this",symbol.getIndex());
        }else {
            writePop(symbol.getKind(),symbol.getIndex());
        }
    }

    public void writePushConst(int constant) throws IOException {
        writePush("constant",constant);
    }

    public void close() throws IOException {
        outputFileWriter.flush();
        outputFileWriter.close();
    }
}
