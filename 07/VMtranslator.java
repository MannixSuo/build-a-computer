package com.example.demo.vmtranslater;

import java.io.*;

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
                if (command.contains("//")) continue;
                Command commandType = Parser.commandType(command);
                if (commandType == Command.C_ARITHMETIC) {
                    String arg1 = Parser.arg1(command);
                    codeWriter.writeArithmetic(arg1);
                } else if (commandType == Command.C_POP || commandType == Command.C_PUSH) {
                    String arg1 = Parser.arg1(command);
                    int arg2 = Parser.arg2(command);
                    codeWriter.writePushPop(commandType, arg1, arg2);
                }
            }
        }
    }
}
