import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
    private BufferedWriter outputFileWriter;

    public VMWriter(File outputFile) throws IOException {
        this.outputFileWriter = new BufferedWriter(new FileWriter(outputFile));
    }

    public void writePush(String segments, int index) throws IOException {
        outputFileWriter.write(String.format("push %s %d", segments, index));
    }

    public void writePop(String segments, int index) throws IOException {
        outputFileWriter.write(String.format("pop %s %d", segments, index));
    }

    public void writeArithmetic(Symbol command) throws IOException {
        outputFileWriter.write(command.getValue());
    }

    public void writeLabel(String label) throws IOException {
        outputFileWriter.write(String.format("label %s", label));
    }

    public void writeGoto(String label) throws IOException {
        outputFileWriter.write(String.format("goto %s", label));
    }

    public void writeIf(String label) throws IOException {
        outputFileWriter.write(String.format("if-goto %s", label));
    }

    public void writeCall(String name, int nArgs) throws IOException {
        outputFileWriter.write(String.format("call %s %d", name, nArgs));
    }

    public void writeFunction(String name,int nLocals) throws IOException {
        outputFileWriter.write(String.format("function %s %d",name,nLocals));
    }
    public void writeReturn() throws IOException {
        outputFileWriter.write("return");
    }
    public void close() throws IOException {
        outputFileWriter.flush();
        outputFileWriter.close();
    }
}
