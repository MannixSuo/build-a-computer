// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// Put your code here.


(KB)
@KBD
D=M
@BLACK
D;JNE   // if any key pressed D not equals to 0 just jump to BLACK 
@WHITE
D;JEQ   // else jump to white
		
(BLACK) // black the screen
@SCREEN
D=A
@i
M=D    // store the value for the next address whic should be -1 (black)
@c
M=0    // c=0 used to control the loop that not out of range of screen
(BSTART)
@i
D=M 
A=D
M=-1 // ram[i]=-1 black all the 16 pixels
@i
M=D+1 // i=i+1
@c
M=M+1 //c=c+1
D=M
@8192 //8192 the max size of screen
D=D-A
@BSTART
D;JLT
@KB
0;JEQ

(WHITE)
@SCREEN
D=A
@i
M=D
@c
M=0
(WSTART)
@i
D=M
A=D
M=0
@i
M=D+1
@c
M=M+1
D=M
@8192
D=D-A
@WSTART
D;JLT
@KB
0;JEQ