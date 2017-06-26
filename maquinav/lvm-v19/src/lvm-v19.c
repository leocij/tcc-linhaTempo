/*
 * lvm-v19.c
 *
 *  Created on: 26 de mar de 2017
 *      Author: leoci
 */

#include <stdio.h>
#include <stdlib.h>

int retornaInt(unsigned char *buffer, int x);
void imprimeMemoria(int inicio, int fim, unsigned char *buffer);

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
		int indice = 0;

		//Palavra chava "baba babe" em hex.
		if (((int) (buffer[indice++]) == 186)
				&& ((int) (buffer[indice++]) == 186)
				&& ((int) (buffer[indice++]) == 186)
				&& ((int) (buffer[indice++]) == 190)) {

			int tamInstrucoes = retornaInt(buffer, indice);
			indice += 4;

			int tamDados = retornaInt(buffer, indice);
			indice += 4;
			tamDados += 0;

			int inicioInstrucoes = indice;

			int inicioDados = inicioInstrucoes + tamInstrucoes;

			int indiceMemoria;
			char strControl[10000];
			int saida;

			while (indice < size) {
				//printf("passei indice = %d\n", indice);

				if (buffer[indice] == 0x01) {
					indice++;
					if (buffer[indice] == 0x01) {
						indice++;
						indiceMemoria = retornaInt(buffer, indice);
						indice += 4;
						//printf("%c", buffer[inicioDados + indiceMemoria]);
						int x = 0;
						int y = inicioDados + indiceMemoria;
						while (buffer[y] != '\0') {
							strControl[x++] = buffer[y++];
						}
						strControl[x] = '\0';

						//printf("%s", strControl);
					}

				} else if (buffer[indice] == 0x02) {
					indice++;
					if (buffer[indice] == 0x01) {
						indice++;
						saida = retornaInt(buffer, indice);
						indice += 4;
						if (saida == 0) {
							printf("%s", strControl);
						}
					}

				} else if (buffer[indice] == 0x0c) {
					indice++;
					if (buffer[indice] == 0x01) {
						indice++;
						indiceMemoria = retornaInt(buffer, indice);
						indice += 4;
						//printf("%c", buffer[inicioDados + indiceMemoria]);
						int x = 0;
						int y = inicioDados + indiceMemoria;
						char concat[10000];
						while (buffer[y] != '\0') {
							concat[x++] = buffer[y++];
						}

						int k = 0;
						while (strControl[k] != '\0') {
							concat[x++] = strControl[k++];
						}
						concat[x] = '\0';

						strControl[0] = '\0';
						int m = 0;
						int n = 0;
						while (concat[m] != '\0') {
							strControl[n++] = concat[m++];
						}
						strControl[n] = '\0';

						//printf("%s", strControl);
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
