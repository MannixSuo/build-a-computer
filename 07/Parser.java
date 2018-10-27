package com.example.demo.vmtranslater;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles the parsing of a single .vm file,
 * and encapsulates access to the input code.
 * It reads VM commands, parses them,
 * and provides convenient access to their components.
 * In addition, it removes all white space and comments.
 */
public class Parser {


    private static String[] arith = {"add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not"};
    private static List<String> arithmeticCommands = new ArrayList<>(Arrays.asList(arith));
    private BufferedReader bufferedReader;
    private FileReader fileReader;
    private String currentCommand;

    public Parser() {
    }

    /**
     * Opens the input file/stream and gets ready to parse it.
     *
     * @param bufferedReader file
     */
    public Parser(BufferedReader bufferedReader) {
        this.bufferedReader = bufferedReader;
    }

    /**
     * Are there more commands in the input?
     */
    public boolean hasMoreCommand() throws IOException {
        currentCommand = bufferedReader.readLine();
        return currentCommand != null;
    }

    /**
     * Reads the next command from the input and makes it the current command.
     * Should be called only if hasMoreCommands is true.
     * Initially there is no current command.
     */
    String advance() {
        return currentCommand;
    }

    static Command commandType(String command) {
        String[] commands = command.split(" ");
        String commandType = commands[0];
        if (commands.length == 1) {
            if (arithmeticCommands.contains(commandType)) {
                return Command.C_ARITHMETIC;
            }
        } else if (commands.length == 3) {
            if (commandType.equals("pop")) {
                return Command.C_POP;
            } else if (commandType.equals("push")) {
                return Command.C_PUSH;
            }
        }
        return null;
    }

    /*
    Returns the first arg. of the current command.In the case of C_ARITHMETIC,
    the command itself (add, sub, etc.) is returned.
    Should not be called if the current command is C_RETURN
     */
    public static String arg1(String command) {
        String[] commands = command.split(" ");
        if (commands.length > 1) {
            return commands[1];
        }
        return commands[0];
    }

    /*
    Returns the second argument of the current command.
    Should be called only if the current command is C_PUSH, C_POP, C_FUNCTION, or C_CALL.
     */
    public static int arg2(String command) {
        String[] commands = command.split(" ");
        return Integer.parseInt(commands[2]);
    }

}
