@echo off
java -jar scp8.jar %1
type saida.c
gcc -o saida.exe saida.c