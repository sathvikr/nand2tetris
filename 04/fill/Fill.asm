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

// while (true) {
//    if (key == 0) {
//        color = 0
//    } else {
//        color = -1
//    }

//    for (i = 0; i < 8192; i+=32) {
//        SCREEN[i] = color
//    }
// }

(LOOP)
    @KEY
    D=M
    @i
    M=0
    @WHITE
    D;JEQ

// Set color to -1 if key != 0
    @color
    M=-1

(FILL)
    @8192
    D=A
    @i
    D=D-M
    @LOOP
    D;JEQ

// Set each bit in screen memory map to color.
    @SCREEN
    D=A
    @i
    D=D+M
    @address
    M=D
    @color
    D=M
    @address
    A=M
    M=D

    @i
    M=M+1

    @FILL
    0;JMP

// Set color to 0 if key == 0
(WHITE)
    @color
    M=0
    @FILL
    0;JMP
