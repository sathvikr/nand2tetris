// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)
//
// This program only needs to handle arguments that satisfy
// R0 >= 0, R1 >= 0, and R0*R1 < 32768.

// Put your code here

// If RAM[0] > RAM[1], jump to swapping code.
//    @R0
//    D=M
//    @R1
//    D=D-M
//    @GREATER
//    D;JGT

// Initialize product = 0, i = 0.
(MULTIPLY)
    @product
    M=0

    @i
    M=0

// Jump to end if RAM[0] - i == 0, or in other words, if i == RAM[0].
(LOOP)
    @R0
    D=M
    @i
    D=D-M
    @STORE
    D;JEQ

// Add RAM[1] to product.
    @R1
    D=M
    @product
    M=M+D

// Increment i by 1.
    @i
    M=M+1
    @LOOP
    M;JMP

// Swap RAM[0] and RAM[1].
// (GREATER)
//    @R0
//    D=M
//    @temp
//    M=D
//    @R1
//    D=M
//    @R0
//    M=D

//    @temp
//    D=M
//    @R1
//    M=D

//    @MULTIPLY
//    0;JMP

// Set RAM[2] to answer.
(STORE)
    @product
    D=M
    @R2
    M=D

(END)
    0;JMP