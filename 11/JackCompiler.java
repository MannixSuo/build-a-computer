import java.io.File;
import java.io.IOException;

public class JackCompiler {
    public static void main(String[] args) throws IOException {
        String fileName ;
        // fileName = args[0];
        fileName =  "E:\\build-a-computer\\11\\Test.jack";
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
            JackTokenizer tokenizer = new JackTokenizer(currentFile);
            File outputFile = new File(currentFile.getAbsolutePath().replace(".jack",".vm"));
            CompilationEngine compilationEngine = new CompilationEngine(tokenizer,outputFile);
            compilationEngine.compile();
            compilationEngine.writeOutputFile();
            tokenizer.removeCacheFile();
        }
    }
}
