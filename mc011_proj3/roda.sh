#!/bin/sh

java main.Main < test/"$1"
mv minijava.asm "$1".asm
nasm -felf "$1".asm
gcc runtime.o "$1".o
mv a.out "$1".out
./"$1".out
