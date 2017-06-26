/*
 * lvm-v7.c
 *
 *  Created on: 13 de mar de 2017
 *      Author: leoci
 */

#include <stdio.h>
#include <stdlib.h>

int retornaInt(unsigned char *buffer, int x);
void imprimeMemoria(int inicio, int fim, unsigned char *buffer);
void insereMem(int reg, unsigned char *buffer, int x);

int main(int argc, char *argv[]) {
	FILE *file;
	size_t size;
	unsigned char *buffer;
	file = fopen(argv[1], "rb");

	fseek(file, 0, SEEK_END);
	size = ftell(file);
	fseek(file, 0, SEEK_SET);
	buffer = (unsigned char *) malloc(size);

	if (file == NULL) {
		printf("Error: There was an Error reading the file %s \n", argv[1]);
		exit(1);
	} else if (fread(buffer, sizeof *buffer, size, file) != size) {
		printf("Error: There was an Error reading the file %s - %d\n", argv[1],
				size);
		exit(1);
	} else {
		//imprimeBuffer(buffer, size);

		int indice = 0;

		//Palavra chava "baba babe" em hex.
		if (((int) (buffer[indice++]) == 186)
				&& ((int) (buffer[indice++]) == 186)
				&& ((int) (buffer[indice++]) == 186)
				&& ((int) (buffer[indice++]) == 190)) {
			int tamInstrucoes = retornaInt(buffer, indice);
			//printf("tamInstrucoes = %d\n", tamInstrucoes);
			//printf("indiceReal = %d\n", indice);
			indice += 4;

			int tamDados = retornaInt(buffer, indice);
			//printf("tamDados = %d\n", tamDados);
			//printf("indiceReal = %d\n", indice);
			indice += 4;
			tamDados += 0;

			int inicioInstrucoes = indice;
//			printf("inicioInstrucoes = %d\n", inicioInstrucoes);
//			printf("indiceReal = %d\n", indice);

			int inicioDados = inicioInstrucoes + tamInstrucoes;
//			printf("inicioDados = %d\n", inicioDados);
//			printf("indiceReal = %d\n", indice);

			int indiceMemoria;
			int rInt;

			while (indice < size) {
				if (buffer[indice] == 0x01) {
					// mov rInt, mInt
					indice++;
					if (buffer[indice] == 0x01) {
						indice++;
						indiceMemoria = retornaInt(buffer, indice);
						indice += 4;
						indiceMemoria += inicioInstrucoes + tamInstrucoes;
						rInt = retornaInt(buffer, indiceMemoria);
						//printf("rInt = %d\n", rInt);
					}

				} else if (buffer[indice] == 0x02) {
					// add rInt, mInt
					indice++;
					if (buffer[indice] == 0x01) {
						indice++;
						indiceMemoria = retornaInt(buffer, indice);
						indice += 4;
						indiceMemoria += inicioInstrucoes + tamInstrucoes;
						rInt += retornaInt(buffer, indiceMemoria);
						//printf("rInt = %d\n", rInt);
					}

				} else if (buffer[indice] == 0x03) {
					// mov mInt, rInt
					indice++;
					indiceMemoria = retornaInt(buffer, indice);
					indice += 4;

					if (buffer[indice] == 0x01) {
						indice++;
						indiceMemoria += inicioInstrucoes + tamInstrucoes;
						insereMem(rInt, buffer, indiceMemoria);
						//printf("rInt = %d\n", rInt);
					}

				} else if ((int) (buffer[indice++]) == 255) {
					printf("\nEstado final da Memoria.\n");
					imprimeMemoria(inicioDados, size - 1, buffer);
					printf("\n");
					exit(0);
				}
			}

		} else {
			printf(
					"Codigo da Leoci Virtual Machine nao encontrado no arquivo %s!\n",
					argv[1]);
			exit(0);
		}
	}

	return 0;
}

int retornaInt(unsigned char *buffer, int x) {

	//printf("X = %d\n", x);
	int a = buffer[x++];
	int b = buffer[x++];
	int c = buffer[x++];
	int d = buffer[x++];

	//printf("a = %d, b = %d, c = %d, d = %d\n", a, b, c, d);

	return ((a << 24) + (b << 16) + (c << 8) + d);

	return 0;
}

void imprimeMemoria(int inicio, int fim, unsigned char *buffer) {
	for (int i = inicio; i < fim; i++) {
		printf("%02X ", buffer[i]);
	}
}

void insereMem(int reg, unsigned char *buffer, int x) {

	buffer[x++] = reg >> 24;
	//printf("%02X-1\n", buffer[x - 1]);
	buffer[x++] = reg >> 16;
	//printf("%02X-2\n", buffer[x - 1]);
	buffer[x++] = reg >> 8;
	//printf("%02X-3\n", buffer[x - 1]);
	buffer[x++] = reg;
	//printf("%02X-4\n", buffer[x - 1]);
}
