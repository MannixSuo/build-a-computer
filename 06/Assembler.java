package assembler;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class Assembler {
	static int lineNumber = 0;
	public static void main(String[] args) throws IOException {
		String path = "D:\\nand2tetris\\projects\\06\\rect\\";
		String fileName = "RectL";
		String filePath = path + fileName+".asm";
		File file = new File(filePath);
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line;
		bufferedReader.mark((int)file.length()+1);
		while(( line = bufferedReader.readLine())!=null) {
			if(line.contains("//")) {
				line = line.substring(0, line.indexOf("/"));
			}
			line = line.replaceAll(" ", "");
			if(line.isEmpty())continue;
			if(line.contains("(")) {
				String symble = line.substring(1, line.length()-1);
				symbols.put(symble, lineNumber);
			}else {
				lineNumber++;
			}
		}
		String wirteFilePath = path+fileName+".hack";
		File writeFile = new File(wirteFilePath);
		if(!writeFile.exists()) {
			writeFile.createNewFile();
		}
		FileOutputStream fileOutputStream = new FileOutputStream(writeFile);
		bufferedReader.reset();
		while(( line = bufferedReader.readLine())!=null) {
			String cmd = null;
			if(line.contains("//")) {
				line = line.substring(0,line.indexOf("//"));
			}
			line = line.replaceAll(" ", "");
			System.out.println(line);
			if(line.isEmpty()||line.contains("("))continue;
			System.out.println("instruction "+line);
			if(line.contains("@")) {
				cmd = dealwithAinstrucetion(line);
			}else {
				cmd = dealwithCinstruction(line);
			}
			cmd =cmd+"\n";
			System.out.println("hack:"+cmd);
			fileOutputStream.write(cmd.getBytes());
			
		}
		fileOutputStream.close();
	}

	/**
	 * @param line
	 * @return
	 */
	private static String dealwithCinstruction(String line) {
		// TODO Auto-generated method stub
		String comp = "0000000",jump = "000",dest = "000";
		String[] dAndcompAndJmp =  line.split(";");
		if(dAndcompAndJmp.length>1) {
			jump = dealWithJump(dAndcompAndJmp[1]);
		}
		String[] destAndComp = dAndcompAndJmp[0].split("=");
		if(destAndComp.length>1) {
			dest = destAndComp[0];
			comp = destAndComp[1];
			comp = dealWithComp(comp);
			dest =  dealWithDest(dest);
		}else {
			comp = destAndComp[0];
			comp = dealWithComp(comp);
		}
		return "111"+comp+dest+jump;
	}

	private static String dealWithDest(String dest) {
		// TODO Auto-generated method stub
		String d1="0",d2="0",d3="0";
		if(dest.contains("M")) {d3="1";}
		if(dest.contains("D")) {d2="1";}
		if(dest.contains("A")) {d1="1";}
		return d1+d2+d3;
	}

	private static String dealWithComp(String comp) {
		// TODO Auto-generated method stub
		String a;
		if(comp.contains("A")||comp.equals("0")||comp.equals("1")
				||comp.equals("-1")||comp.equals("1")
				||comp.equals("D")||comp.equals("!D")
				||comp.equals("-D")||comp.equals("D+1")
				||comp.equals("D-1")){
			a="0";
		}else {
			a="1";
		}
		String ad=null;
		if(comp.equals("0")) {ad = "101010";}
		if(comp.equals("1")) {ad = "111111";}
		if(comp.equals("-1")) {ad = "111010";}
		if(comp.equals("D")) {ad = "001100";}
		if(comp.equals("A")||comp.equals("M")) {ad = "110000";}
		if(comp.equals("!D")) {ad = "001101";}
		if(comp.equals("!A")||comp.equals("!M")) {ad = "110001";}
		if(comp.equals("-D")) {ad = "001111";}
		if(comp.equals("-A")||comp.equals("-M")) {ad = "110011";}
		if(comp.equals("D+1")) {ad = "011111";}
		if(comp.equals("A+1")||comp.equals("M+1")) {ad = "110111";}
		if(comp.equals("D-1")) {ad = "001110";}
		if(comp.equals("A-1")||comp.equals("M-1")) {ad = "110010";}
		if(comp.equals("D+A")||comp.equals("D+M")) {ad = "000010";}
		if(comp.equals("D-A")||comp.equals("D-M")) {ad = "010011";}
		if(comp.equals("A-D")||comp.equals("M-D")) {ad = "000111";}
		if(comp.equals("D&A")||comp.equals("D&M")) {ad = "000000";}
		if(comp.equals("D|A")||comp.equals("D|M")) {ad = "010101";}
		return a+ad;
	}

	private static String dealWithJump(String string) {
		// TODO Auto-generated method stub
		if("JGT".equals(string)) {
			return "001";
		}
		if("JEQ".equals(string)) {
			return "010";
		}
		if("JGE".equals(string)) {
			return "011";
		}
		if("JLT".equals(string)) {
			return "100";
		}
		if("JNE".equals(string)) {
			return "101";
		}
		if("JLE".equals(string)) {
			return "110";
		}
		if("JMP".equals(string)) {
			return "111";
		}
		return "000";
	}
	static int i = 0;
	static Map<String,Integer> symbols = initialMap();
	private static String dealwithAinstrucetion(String line) {
		// TODO Auto-generated method stub
		String binaryValue = line.substring(line.indexOf("@")+1, line.length());
		boolean notSymbol = binaryValue.matches("\\d+");
		Integer value =null;
		if(!notSymbol) {
			if(symbols.containsKey(binaryValue)) {
				value = symbols.get(binaryValue);
				System.out.println("huoqu "+binaryValue+"的值："+value);
			}else {
				value = 16+i;
				symbols.put(binaryValue, value);
				i++;
				System.out.println("设置 "+binaryValue+"的值："+value);
			}
		}else {
			value = Integer.parseInt(binaryValue);
		}
		String b = Integer.toBinaryString(value);
		// ��0
		while (b.length()<16) {
			b="0"+b;
		}
		return b ;
	}

	private static Map<String, Integer> initialMap() {
		// TODO Auto-generated method stub
		Map<String,Integer> symbols = new HashMap<>();
		symbols.put("SP", 0);
		symbols.put("LCL", 1);
		symbols.put("ARG", 2);
		symbols.put("THIS", 3);
		symbols.put("THAT", 4);
		symbols.put("R0", 0);
		symbols.put("R1", 1);
		symbols.put("R2", 2);
		symbols.put("R3", 3);
		symbols.put("R4", 4);
		symbols.put("R5", 5);
		symbols.put("R6", 6);
		symbols.put("R7", 7);
		symbols.put("R8", 8);
		symbols.put("R9", 9);
		symbols.put("R10", 10);
		symbols.put("R11", 11);
		symbols.put("R12", 12);
		symbols.put("R13", 13);
		symbols.put("R14", 14);
		symbols.put("R15", 15);
		symbols.put("SCREEN", 16384);
		symbols.put("KBD", 24576);
		return symbols;
	}
}
