/*
 * lvm-v23.c
 *
 *  Created on: 31 de mar de 2017
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
			int strCtrlIndice = 0;
			strControl[strCtrlIndice] = '\0';
			int saida;
			int intControl = 0;
			int intComp = 0;

			while (indice < size) {
				//printf("passei indice = %d\n", indice);

				if (buffer[indice] == 0x01) {
					// mov-strControl-indiceMem
					indice++;
					if (buffer[indice] == 0x01) {
						indice++;
						indiceMemoria = retornaInt(buffer, indice);
						indice += 4;
						//printf("%c", buffer[inicioDados + indiceMemoria]);
						//int x = 0;
						int y = inicioDados + indiceMemoria;
						while (buffer[y] != '\0') {
							strControl[strCtrlIndice++] = buffer[y++];
						}
						strControl[strCtrlIndice] = '\0';

						//printf("mov-strControl-indiceMem = %s", strControl);
					}

				} else if (buffer[indice] == 0x02) {
					// int-saida-strControl
					indice++;
					if (buffer[indice] == 0x01) {
						indice++;
						saida = retornaInt(buffer, indice);
						indice += 4;
						if (saida == 0) {
							printf("%s", strControl);
							strCtrlIndice = 0;
							strControl[strCtrlIndice] = '\0';
						}
					}

				} else if (buffer[indice] == 0x03) {
					indice++;
					if (buffer[indice] == 0x04) {
						indice++;
						int bufferInt;
						scanf("%d", &bufferInt);
						indiceMemoria = retornaInt(buffer, indice);
						indice += 4;
						insereMem(bufferInt, buffer,
								inicioDados + indiceMemoria);

					}

				} else if (buffer[indice] == 0x04) {
					// mov-indiceMem-intControl
					indice++;
					if (buffer[indice] == 0x02) {
						indice++;
						indiceMemoria = retornaInt(buffer, indice);
						indice += 4;
						insereMem(intControl, buffer,
								inicioDados + indiceMemoria);

					}

				} else if (buffer[indice] == 0x05) {
					// mov-intControl-indiceMem
					indice++;
					if (buffer[indice] == 0x02) {
						indice++;
						indiceMemoria = retornaInt(buffer, indice);
						indice += 4;
						intControl = retornaInt(buffer,
								inicioDados + indiceMemoria);
					}

				} else if (buffer[indice] == 0x06) {
					// mov-intControl-intNumber
					indice++;
					if (buffer[indice] == 0x02) {
						indice++;
						intControl = retornaInt(buffer, indice);
						indice += 4;
					}

				} else if (buffer[indice] == 0x07) {
					// add-intControl-indiceMem
					indice++;
					if (buffer[indice] == 0x02) {
						indice++;
						indiceMemoria = retornaInt(buffer, indice);
						indice += 4;
						intControl += retornaInt(buffer,
								inicioDados + indiceMemoria);
					}

				} else if (buffer[indice] == 0x08) {
					// sub-intControl-indiceMem
					indice++;
					if (buffer[indice] == 0x02) {
						indice++;
						indiceMemoria = retornaInt(buffer, indice);
						indice += 4;
						intControl -= retornaInt(buffer,
								inicioDados + indiceMemoria);
					}

				} else if (buffer[indice] == 0x09) {
					// mult-intControl-indiceMem
					indice++;
					if (buffer[indice] == 0x02) {
						indice++;
						indiceMemoria = retornaInt(buffer, indice);
						indice += 4;
						intControl *= retornaInt(buffer,
								inicioDados + indiceMemoria);
					}

				} else if (buffer[indice] == 0x0a) {
					// div-intControl-indiceMem
					indice++;
					if (buffer[indice] == 0x02) {
						indice++;
						indiceMemoria = retornaInt(buffer, indice);
						indice += 4;
						int saiZero = retornaInt(buffer,
								inicioDados + indiceMemoria);
						if (saiZero == 0) {
							printf("Erro de divisao por zero!\n");
						} else {
							saiZero = intControl / saiZero;
							intControl = saiZero;
						}
					}

				} else if (buffer[indice] == 0x0b) {
					// convert-strControl-indiceMem
					indice++;
					if (buffer[indice] == 0x01) {
						indice++;
						indiceMemoria = retornaInt(buffer, indice);
						indice += 4;
						int nInt = retornaInt(buffer,
								inicioDados + indiceMemoria);

						char str[20];
						sprintf(str, "%d", nInt);

						int m = 0;
						//int n = 0;
						while (str[m] != '\0') {
							strControl[strCtrlIndice++] = str[m++];
						}
						strControl[strCtrlIndice] = '\0';
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

						int z = 0;
						while (strControl[z] != '\0') {
							concat[x++] = strControl[z++];
						}
						concat[x] = '\0';

						strControl[0] = '\0';
						int m = 0;
						strCtrlIndice = 0;
						while (concat[m] != '\0') {
							strControl[strCtrlIndice++] = concat[m++];
						}
						strControl[strCtrlIndice] = '\0';

						//printf("%s", strControl);
					}

				} else if (buffer[indice] == 0x0d) {
					// cmp-intControl-indiceMem
					indice++;
					if (buffer[indice] == 0x02) {
						indice++;

						indiceMemoria = retornaInt(buffer, indice);
						indice += 4;

						int nInt = retornaInt(buffer,
								inicioDados + indiceMemoria);
						intComp = nInt - intControl;

					}

				} else if (buffer[indice] == 0x0e) {
					// cmp-intControl-intNumber
					indice++;
					if (buffer[indice] == 0x02) {
						indice++;
						intComp = retornaInt(buffer, indice) - intControl;
						indice += 4;
					}

				} else if (buffer[indice] == 0x10) {
					indice++;
					// JUMP

					if (buffer[indice] == 0x01) {
						indice++;
						// jg

						indiceMemoria = retornaInt(buffer, indice);
						indice += 4;

						if (intComp > 0) {
							indice += retornaInt(buffer,
									inicioDados + indiceMemoria);
						}
					} else if (buffer[indice] == 0x02) {
						indice++;
						// jge

						indiceMemoria = retornaInt(buffer, indice);
						indice += 4;

						if (intComp >= 0) {
							indice += retornaInt(buffer,
									inicioDados + indiceMemoria);
						}
					} else if (buffer[indice] == 0x03) {
						indice++;
						// jl

						indiceMemoria = retornaInt(buffer, indice);
						indice += 4;

						if (intComp < 0) {
							indice += retornaInt(buffer,
									inicioDados + indiceMemoria);
						}
					} else if (buffer[indice] == 0x04) {
						indice++;
						// jle

						indiceMemoria = retornaInt(buffer, indice);
						indice += 4;

						if (intComp <= 0) {
							indice += retornaInt(buffer,
									inicioDados + indiceMemoria);
						}
					} else if (buffer[indice] == 0x05) {
						indice++;
						// je

						indiceMemoria = retornaInt(buffer, indice);
						indice += 4;

						if (intComp == 0) {
							indice += retornaInt(buffer,
									inicioDados + indiceMemoria);
						}
					} else if (buffer[indice] == 0x06) {
						indice++;
						// jne

						indiceMemoria = retornaInt(buffer, indice);
						indice += 4;

						if (intComp != 0) {
							indice += retornaInt(buffer,
									inicioDados + indiceMemoria);
						}
					} else if (buffer[indice] == 0x07) {
						indice++;
						// fimse

						indiceMemoria = retornaInt(buffer, indice);
						indice += 4;

						indice += retornaInt(buffer,
								inicioDados + indiceMemoria);
					} else if (buffer[indice] == 0x08) {
						indice++;
						// laco

						int intLaco = retornaInt(buffer, indice);
						indice += 4;

						indice -= intLaco;
					}

					// FIM DOS JUMPS

				} else if (buffer[indice] == 0x11) {
					indice++;
					if (buffer[indice] == 0x01) {
						indice++;
						int nInt = retornaInt(buffer, indice);
						indice += 4;

						char str[20];
						sprintf(str, "%d", nInt);

						int m = 0;
						//int n = 0;
						while (str[m] != '\0') {
							strControl[strCtrlIndice++] = str[m++];
						}
						strControl[strCtrlIndice] = '\0';
					}

				} else if (buffer[indice] == 0x12) {
					// concat-strControlConvert-indiceMem
					indice++;
					if (buffer[indice] == 0x01) {
						indice++;
						indiceMemoria = retornaInt(buffer, indice);
						indice += 4;

						int nInt = retornaInt(buffer,
								inicioDados + indiceMemoria);

						char str[20];
						sprintf(str, "%d", nInt);

						//printf("str = %s\n", str);

						char concat[10000];
						int concatIndice = 0;
						int strIndice = 0;

						while (str[strIndice] != '\0') {
							concat[concatIndice++] = str[strIndice++];
						}

						int strCtrlIndiceTemp = 0;

						while (strControl[strCtrlIndiceTemp] != '\0') {
							concat[concatIndice++] =
									strControl[strCtrlIndiceTemp++];
						}
						concat[concatIndice] = '\0';

						//printf("concat = %s\n", concat);

						strControl[0] = '\0';
						concatIndice = 0;
						strCtrlIndice = 0;
						while (concat[concatIndice] != '\0') {
							strControl[strCtrlIndice++] =
									concat[concatIndice++];
						}
						strControl[strCtrlIndice] = '\0';
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

// Inicio das Funções

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
