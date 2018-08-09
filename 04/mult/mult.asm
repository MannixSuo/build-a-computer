// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

// Put your code here.
//  i=0
//  while i < R1
//  R2 += R0
//  i++
@i
M=0     //i=0
@R2
M=0
@R0
D=M
@END
D;JEQ // if R0 == 0 goto end
@R1
D=M
@END
D;JEQ // if R1 == 0 goto end

(START)
@i
D=M
@R1
D=D-M 
@END
D;JGE // if i-R1<=0 goto end

@i
M=M+1  // i++

@R0
D=M  
@R2
M=D+M  //R2+=R0
@START
0;JEQ
(END)
@END
0;JEQ
