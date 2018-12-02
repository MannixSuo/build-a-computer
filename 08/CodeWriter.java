
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Translates VM commands into Hack assembly code.
 */
public class CodeWriter {
	private BufferedWriter bufferedWriter;
	private static int CODE_FLAG1, CODE_FLAG2;

	/*
	 * opens the output file/stream and gets ready to write into it.
	 */
	CodeWriter() {
	}

	/*
	 * Informs the code writer that the translation of a new VM file is started. set
	 * sp=256
	 */
	void setFileName(String fileName) {
		File file = new File(fileName);
		try {
			this.bufferedWriter = new BufferedWriter(new FileWriter(file));
			bufferedWriter.write("@256\nD=A\n@SP\nM=D\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Writes the assembly code that is the translation of the given arithmetic
	 * command
	 */
	void writeArithmetic(String command) throws IOException {
		String hackCommand = "";
		if ("add".equals(command)) {
			// @SP
			// M=M-1 sp--
			// A=M
			// D=M d=*sp
			// @SP
			// M=M-1 sp--
			// A=M
			// M=D+M *sp = d+*sp--
			// @SP sp++
			// M=M+1
			hackCommand = "@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nM=D+M\n@SP\nM=M+1\n";
		}
		if ("sub".equals(command)) {
			// @SP
			// M=M-1 sp--
			// A=M
			// D=M d=*sp--
			// @SP
			// M=M-1 sp--
			// A=M
			// M=M-D *sp-- - d
			// @SP
			// M=M+1
			hackCommand = "@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nM=M-D\n@SP\nM=M+1\n";
		}
		if ("neg".equals(command)) {
			// @SP
			// M=M-1 sp--
			// A=M
			// M=-M *sp-- = -*sp--
			// @SP
			// M=M+1
			hackCommand = "@SP\nM=M-1\nA=M\nM=-M\n@SP\nM=M+1\n";
		}
		if ("and".equals(command)) {
			// @SP
			// M=M-1
			// A=M
			// D=M d=*sp--
			// @SP
			// M=M-1
			// A=M
			// M=D&M d&*sp--
			// @SP
			// M=M+1
			hackCommand = "@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nM=D&M\n@SP\nM=M+1\n";
		}
		if ("or".equals(command)) {
			hackCommand = "@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nM=D|M\n@SP\nM=M+1\n";
		}
		if ("not".equals(command)) {
			hackCommand = "@SP\nM=M-1\nA=M\nM=!M\n@SP\nM=M+1\n";
		}
		if ("eq".equals(command)) {
			// @SP
			// M=M-1
			// A=M
			// D=M d=*sp--
			// @SP
			// M=M-1
			// D=D-M d=*sp-- - d
			// @TRUE_CODE_FLAG1
			// D;JEQ
			// D=0
			// @CONTINUE_CODE_FLAG2
			// 0;JEQ
			// (TRUE_CODE_FLAG1)
			// D=-1
			// (CONTINUE CODE_FLAG2)
			// @SP
			// A=M
			// M=D
			// @SP
			// M=M+1
			hackCommand = "@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nD=D-M\n@TRUE_" + CODE_FLAG1 + "\nD;JEQ\nD=0\n"
					+ "@CONTINUE_" + CODE_FLAG2 + "\n0;JMP\n(TRUE_" + CODE_FLAG1 + ")\nD=-1\n(CONTINUE_" + CODE_FLAG2
					+ ")\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
		}
		if ("gt".equals(command)) {
			hackCommand = "@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nD=M-D\n@TRUE_" + CODE_FLAG1 + "\nD;JGT\nD=0\n"
					+ "@CONTINUE_" + CODE_FLAG2 + "\n0;JMP\n(TRUE_" + CODE_FLAG1 + ")\nD=-1\n(CONTINUE_" + CODE_FLAG2
					+ ")\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
		}
		if ("lt".equals(command)) {
			hackCommand = "@SP\nM=M-1\nA=M\nD=M\n@SP\nM=M-1\nA=M\nD=M-D\n@TRUE_" + CODE_FLAG1 + "\nD;JLT\nD=0\n"
					+ "@CONTINUE_" + CODE_FLAG2 + "\n0;JMP\n(TRUE_" + CODE_FLAG1 + ")\nD=-1\n(CONTINUE_" + CODE_FLAG2
					+ ")\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
		}
		bufferedWriter.write(hackCommand);
		CODE_FLAG1++;
		CODE_FLAG2++;
	}

	/*
	 * rites the assembly code that is the translation of the given command, where
	 * commandis either C_PUSH or C_POP.
	 * 
	 */
	void writePushPop(Command command, String segment, int index,String fileName) throws IOException {
		String hackCommand = "";
		if (command == Command.C_PUSH) {
			if ("constant".equals(segment)) {
				hackCommand = "@" + index + "\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
			}
			if ("local".equals(segment)) {
				hackCommand = "@LCL\nD=M\n@" + index + "\nA=A+D\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
			}
			if ("argument".equals(segment)) {
				hackCommand = "@ARG\nD=M\n@" + index + "\nA=A+D\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
			}
			if ("this".equals(segment)) {
				hackCommand = "@THIS\nD=M\n@" + index + "\nA=A+D\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
			}
			if ("that".equals(segment)) {
				hackCommand = "@THAT\nD=M\n@" + index + "\nA=A+D\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
			}
			if ("pointer".equals(segment)) {
				if (index == 0) {
					hackCommand = "@3\n";
				}
				if (index == 1) {
					hackCommand = "@4\n";
				}
				hackCommand += "D=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
			}
			if ("static".equals(segment)) {
				String staticName = fileName.substring(0, fileName.lastIndexOf(".vm"))+"."+index;
				hackCommand ="@"+staticName+"\nD=A\nA=D\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
			}
			if ("temp".equals(segment)) {
				hackCommand = "@" + index + "\nD=A\n@5\nD=A+D\nA=D\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
			}
		}
		if (command == Command.C_POP) {
			if ("local".equals(segment)) {
				// @index
				// D=A
				// @LCL
				// D=D+M
				// @R13
				// M=D R13=LCL+index
				// save pop index in R13
				// @SP
				// M=M-1
				// A=M
				// D=M d=*sp--
				// @R13
				// A=M
				// M=D *R13=d
				hackCommand = "@" + index + "\nD=A\n@LCL\nD=D+M\n@R13\nM=D\n@SP\nM=M-1\nA=M\nD=M\n@R13\nA=M\nM=D\n";
			}
			if ("argument".equals(segment)) {
				hackCommand = "@" + index + "\nD=A\n@ARG\nD=D+M\n@R13\nM=D\n@SP\nM=M-1\nA=M\nD=M\n@R13\nA=M\nM=D\n";
			}
			if ("this".equals(segment)) {
				hackCommand = "@" + index + "\nD=A\n@THIS\nD=D+M\n@R13\nM=D\n@SP\nM=M-1\nA=M\nD=M\n@R13\nA=M\nM=D\n";
			}
			if ("that".equals(segment)) {
				hackCommand = "@" + index + "\nD=A\n@THAT\nD=D+M\n@R13\nM=D\n@SP\nM=M-1\nA=M\nD=M\n@R13\nA=M\nM=D\n";
			}
			if ("pointer".equals(segment)) {
				hackCommand = "@SP\nM=M-1\nA=M\nD=M\n";
				if (index == 0) {
					hackCommand += "@3\n";
				} else {
					hackCommand += "@4\n";
				}
				hackCommand += "M=D\n";
			}
			if ("static".equals(segment)) {
				// @index
				// D=A
				// @16
				// D=D+A
				// @R13
				// M=D
				// @SP
				// M=M-1
				// A=M
				// D=M
				// @R13
				// A=M
				// M=D
				String staticName = fileName.substring(0, fileName.lastIndexOf(".vm"))+"."+index;
				hackCommand = "@SP\nM=M-1\nA=M\nD=M\n@"+staticName+"\nM=D\n";
			}
			if ("temp".equals(segment)) {
				hackCommand = "@" + index + "\nD=A\n@5\nD=D+A\n@R13\nM=D\n@SP\nM=M-1\nA=M\nD=M\n@R13\nA=M\nM=D\n";
			}
		}
		bufferedWriter.write(hackCommand);
	}

	public void writeLabel(String arg1) throws IOException {
		// (arg1)
		bufferedWriter.write("(" + arg1 + ")\n");
	}

	public void writeGoto(String arg1) throws IOException {
		// @arg1
		// 0;JMP
		bufferedWriter.write("@" + arg1 + "\n0;JMP\n");
	}

	public void writeIf(String arg1) throws IOException {
		// @SP
		// M=M-1 sp--
		// A=M
		// D=M d=top most value
		// @arg1
		// D;JNE jump if d is not zero
		bufferedWriter.write("@SP\nM=M-1\nA=M\nD=M\n@" + arg1 + "\nD;JNE\n");
	}

	public void writeCommand(String command) throws IOException {
		bufferedWriter.write("//" + command + "\n");
	}

	/*
	 * Closes the output file.
	 */
	void close() throws IOException {
		bufferedWriter.close();
	}

	public void writeFunction(String functionName, int numLocals) throws IOException {
		// g:
		// repeat nVars times:
		// push 0

		// (functionName)
		// @LCL
		// D=M
		// @SP
		// M=D
		// @SP
		// A=M
		// M=0 *sp=0
		// @SP
		// M=M+1 sp++
		String hackCommand = "(" + functionName + ")\n";
		while (numLocals-- > 0) {
			hackCommand += "@SP\nA=M\nM=0\n@SP\nM=M+1\n";
		}
		bufferedWriter.write(hackCommand);
	}

	public void writeReturn() throws IOException {
		// @LCL
		// D=M
		// @R13
		// M=D r13 = lcl
		// retAddr = *(frame-5)
		// @
		bufferedWriter.write("@LCL\nD=M\n@R13\nM=D\n@5\nD=D-A\nA=D\nD=M"
				+ "\n@R14\nM=D\n@SP\nM=M-1\nA=M\nD=M\n@ARG\nA=M" + "\nM=D\n@ARG\nD=M+1\n@SP\nM=D\n@R13\nD=M\nD=D-1"
				+ "\nA=D\nD=M\n@THAT\nM=D\n@R13\nD=M\nD=D-1\nD=D-1" + "\nA=D\nD=M\n@THIS\nM=D\n@R13\nD=M\nD=D-1\nD=D-1"
				+ "\nD=D-1\nA=D\nD=M\n@ARG\nM=D\n@R13\nD=M\nD=D-1"
				+ "\nD=D-1\nD=D-1\nD=D-1\nA=D\nD=M\n@LCL\nM=D\n@R14\nA=M\n0;JMP\n");
	}

	static int globalReturn = 1;

	public void writeCall(String functionName, int numLocals) throws IOException {
		// @return_address_globalReturn
		// D=A
		// @SP
		// A=M
		// M=D push return address
		// @SP
		// M=M+1 sp++
		// @SP
		// M=M+1 sp++
		// @LCL
		// D=M
		// @SP
		// A=M
		// M=D push lcl
		// @SP
		// M=M+1 sp++
		// @ARG
		// D=M
		// @SP
		// A=M
		// M=D push arg
		// @SP
		// M=M+1 sp++
		// @THIS
		// D=M
		// @SP
		// A=M
		// M=D push this
		// @SP
		// M=M+1 sp++
		// @THAT
		// D=M
		// @SP
		// A=M
		// M=D push that
		// @SP
		// M=M+1 sp++
		// @numLocal
		// D=A
		// @5
		// D=D+A
		// @SP
		// D=M-D
		// @ARG
		// M=D arg = sp-numLocal-5
		// @SP
		// D=M
		// @LCL
		// M=D lcl=sp
		// @functionName
		// 0;JMP
		// (return_address_globalReturn)
		bufferedWriter.write("@return_address_" + globalReturn + "\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1"
				+ "\n@LCL\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n@ARG\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1"
				+ "\n@THIS\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n@THAT\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n" + "@" + numLocals
				+ "\nD=A\n@5\nD=D+A\n@SP\nD=M-D\n@ARG\nM=D\n@SP\nD=M\n@LCL\nM=D\n" + "@" + functionName
				+ "\n0;JMP\n(return_address_" + globalReturn + ")\n");
		globalReturn++;
	}

	public void writeSys() throws IOException {
		bufferedWriter.write("@return_address_0\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n@LCL\nD=M\n@SP\nA=M\nM=D"
				+ "\n@SP\nM=M+1\n@ARG\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n@THIS\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1"
				+ "\n@THAT\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n@0\nD=A\n@5\nD=D+A\n@SP\nD=M-D\n@ARG\nM=D\n@SP\nD=M"
				+ "\n@LCL\nM=D\n@Sys.init\n0;JMP\n(return_address_0)\n");
		
	}
}
