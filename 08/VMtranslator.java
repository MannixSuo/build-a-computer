
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * main of VM translator
 */
public class VMTranslator {
    public static void main(String[] args) throws IOException {
        if (args.length >= 1) {
            String path = args[0];
            File file = new File(path);
        	CodeWriter codeWriter = new CodeWriter();
            if (file.isDirectory()) {
            	String writeFileName = path.contains(File.separator)?path.substring(path.lastIndexOf(File.separator), path.length()):path;
            	codeWriter.setFileName(path + File.separator +writeFileName+ ".asm");
            	String[] files = file.list();
            	List<String> filesArray = new LinkedList<String>(Arrays.asList(files));
            	if(filesArray.contains("Sys.vm")) {
            		Parser parser = new Parser(new BufferedReader(new FileReader(path+File.separator+"Sys.vm")));
            		codeWriter.writeSys();
            		processFile("Sys.vm",codeWriter,parser);
            		filesArray.remove("Sys.vm");
            	}
            	for(String fileName:filesArray) {
            		if(fileName.endsWith(".vm")) {
            			Parser parser = new Parser(new BufferedReader(new FileReader(path+File.separator+fileName)));
                		processFile(fileName,codeWriter,parser);
            		}
            	}
            } else {
            	Parser parser = new Parser(new BufferedReader(new FileReader(file)));
            	codeWriter.setFileName(path.substring(0, path.indexOf(".")) + ".asm");
            	processFile(path,codeWriter,parser);
            }
            codeWriter.close();
        }
    }
    static void processFile(String fileName,CodeWriter codeWriter,Parser parser) throws IOException {
        while (parser.hasMoreCommand()) {
            String command = parser.advance();
            // if start with // continue
            if (command.startsWith("//") || command.length() < 1) continue;
            if (command.contains("//")) {
                command = command.substring(0, command.indexOf("//")).trim();
            }
            Command commandType = Parser.commandType(command);
            codeWriter.writeCommand(command);
            if (commandType == Command.C_ARITHMETIC) {
                String arg1 = Parser.arg1(command);
                codeWriter.writeArithmetic(arg1);
            } else if (commandType == Command.C_POP || commandType == Command.C_PUSH) {
                String arg1 = Parser.arg1(command);
                int arg2 = Parser.arg2(command);
                codeWriter.writePushPop(commandType, arg1, arg2,fileName);
            } else if (commandType == Command.C_LABEL) {
                String arg1 = Parser.arg1(command);
                codeWriter.writeLabel(arg1);
            } else if (commandType == Command.C_GOTO) {
                String arg1 = Parser.arg1(command);
                codeWriter.writeGoto(arg1);
            } else if (commandType == Command.C_IF) {
                String arg1 = Parser.arg1(command);
                codeWriter.writeIf(arg1);
            }else if (commandType==Command.C_FUNCTION){
                String arg1 = Parser.arg1(command);
                int arg2 = Parser.arg2(command);
                codeWriter.writeFunction(arg1,arg2);
            }else if (commandType == Command.C_RETURN){
                codeWriter.writeReturn();
            }else if(commandType ==Command.C_CALL) {
            	String arg1 = Parser.arg1(command);
                int arg2 = Parser.arg2(command);
            	codeWriter.writeCall(arg1,arg2);
            }
        }
    }
    static void processDirectory(String pathName,CodeWriter codeWriter,Parser parser) {
    	 
    }
}
