import java.io.*;

public class JackAnalyzer {
    public static void main(String[] args) throws IOException {
        String fileName = "E:\\build-a-computer\\10\\ExpressionLessSquare\\Main.jack";
        // fileName = args[0];
        File file = new File(fileName);
        File[] files ;
        if (file.isDirectory()){
            files = file.listFiles((dir, name) -> name.endsWith(".jack"));
        }else {
            files = new File[]{file};
        }
        if (files==null){
            return;
        }
        for (File currentFile :files){
            Tokenizer tokenizer = new Tokenizer(currentFile);
            File outputFile = new File(currentFile.getName().replace(".jack","test.xml"));
            CompilationEngine compilationEngine = new CompilationEngine(tokenizer,outputFile);
            compilationEngine.compile();
            compilationEngine.writeOutputFile();
        }
    }
}
