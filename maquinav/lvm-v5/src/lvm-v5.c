/*
 * lvm-v5.c
 *
 *  Created on: 11 de mar de 2017
 *      Author: leoci
 */

#include <stdio.h>
#include <stdlib.h>

void imprimeMemoria(int inicio, int fim, unsigned char *buffer);
int retornaInt(unsigned char *buffer, int x);
void imprimeStr(unsigned char *buffer, int x);
void insereNaMemoria(unsigned char *buffer, int indiceMem, int entrada);

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
		int indiceMem;
		int controlaStrNulo = 0;

		//Palavra chava "baba babe" em hex.
		if (((int) (buffer[indice++]) == 186)
				&& ((int) (buffer[indice++]) == 186)
				&& ((int) (buffer[indice++]) == 186)
				&& ((int) (buffer[indice++]) == 190)) {

			int tamInstrucoes = retornaInt(buffer, indice);
			indice += 4;
			//printf("tamInstrucoes = %d\n", tamInstrucoes);

			int tamDados = retornaInt(buffer, indice);
			indice += 4;
			tamDados += 0;
			//printf("tamDados = %d\n", tamDados);

			int inicioInstrucoes = indice;
			int inicioDados = indice + tamInstrucoes;
			inicioDados += inicioDados;

			while (indice < size) {
				if (buffer[indice] == 0x01) {
					// mov-saida-str
					indice++;
					if (buffer[indice] == 0x01) {
						indice++;
						indiceMem = retornaInt(buffer, indice);
						indice += 4;
						//printf("buffer = %02X\n", buffer[inicioInstrucoes + tamInstrucoes]);
						if (controlaStrNulo++ == 0) {
							imprimeStr(buffer,
									inicioInstrucoes + tamInstrucoes
											+ indiceMem);
						} else {
							imprimeStr(buffer,
									inicioInstrucoes + tamInstrucoes + indiceMem);
						}

					}
				} else if (buffer[indice] == 0x02) {
					// mov-saida-int
					indice++;
					if (buffer[indice] == 0x01) {
						indice++;
						indiceMem = retornaInt(buffer, indice);
						indice += 4;
						printf("%d",
								retornaInt(buffer,
										inicioInstrucoes + tamInstrucoes
												+ indiceMem));
					}
				} else if (buffer[indice] == 0x03) {
					// mov-mem-entrint
					indice++;
					indiceMem = retornaInt(buffer, indice);
					indice += 4;
					if (buffer[indice] == 0x02) {
						indice++;
						int entrada = 0;
						scanf("%d", &entrada);
						insereNaMemoria(buffer,
								inicioInstrucoes + tamInstrucoes + indiceMem,
								entrada);
					}

				} else if ((int) (buffer[indice++]) == 255) {
//					printf("\nEstado final da Memoria.\n");
//					imprimeMemoria(inicioDados, size - 1, buffer);
//					printf("\n");
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
}

void imprimeMemoria(int inicio, int fim, unsigned char *buffer) {
	for (int i = inicio; i < fim; i++) {
		printf("%02X ", buffer[i]);
	}
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

void imprimeStr(unsigned char *buffer, int indiceStr) {
	for (int i = indiceStr; buffer[i] != '\0'; i++) {
		printf("%c", buffer[i]);
		//printf("%02X ", buffer[i]);
	}
	//printf("\n");
}

void insereNaMemoria(unsigned char *buffer, int indiceMem, int entrada) {

	buffer[indiceMem++] = ((entrada >> 24) & 0xFF);
	//printf("buffer = %02X\n", buffer[indiceMem - 1]);
	buffer[indiceMem++] = ((entrada >> 16) & 0xFF);
	//printf("buffer = %02X\n", buffer[indiceMem - 1]);
	buffer[indiceMem++] = ((entrada >> 8) & 0xFF);
	//printf("buffer = %02X\n", buffer[indiceMem - 1]);
	buffer[indiceMem++] = ((entrada >> 0) & 0xFF);
	//printf("buffer = %02X\n", buffer[indiceMem - 1]);

}

