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
(START)
@i
D=M
M=M+1  // i++
@R1
D=M  
@R2
M=D+M  //R2+=R1

@i
D=M
@R1
D=D-M // if i-R1<0 goto START
@START
D;JLT
(END)
@END
0;JEQ
