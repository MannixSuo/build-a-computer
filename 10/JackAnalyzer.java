import java.io.*;

public class JackAnalyzer {
    public static void main(String[] args) throws IOException {
        String fileName ;
        fileName = args[0];
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
            File outputFile = new File(currentFile.getAbsolutePath().replace(".jack",".xml"));
            CompilationEngine compilationEngine = new CompilationEngine(tokenizer,outputFile);
            compilationEngine.compile();
            compilationEngine.writeOutputFile();
            tokenizer.removeCacheFile();
        }
    }
}
