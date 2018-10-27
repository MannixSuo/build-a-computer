
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * main of VM translator
 */
public class VMtranslator {
    public static void main(String[] args) throws IOException {
        if (args.length >= 1) {
            String path = args[0];
            File file = new File(path);
            CodeWriter codeWriter = new CodeWriter();
            if (file.isDirectory()) {
                codeWriter.setFileName(path + ".asm");
            } else {
                codeWriter.setFileName(path.substring(0, path.indexOf(".")) + ".asm");
            }
            Parser parser = new Parser(new BufferedReader(new FileReader(file)));
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
                    codeWriter.writePushPop(commandType, arg1, arg2);
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
                }
            }
            codeWriter.close();
        }
    }
}
