package compilador;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Gerador {

	private No raiz;
	private List<Simbolo> simbolos;
	private List<String> erros = new ArrayList<String>();
	private FileOutputStream printWriter;
	private File arquivo;
	private File arqAsm;
	private PrintWriter printAsm;
	private List<Instrucao> instrucoes = new ArrayList<>();
	private int indiceVar = 0;
	private List<Dado> dados = new ArrayList<>();
	private int[] verdadeAntes = new int[1000];
	private int verdAnt = 0;
	private int pulaSenao = 0;
	private int falAnt = 0;
	private int[] falsoAntes = new int[1000];
	private int pulaFimse;
	private int[] indexIndiceVar = new int[1000];
	private int indVar = 0;
	private int[] indexdd2 = new int[1000];
	private int indDd2 = 0;
	private int[] lacoInstAntes = new int[1000];
	private int laInAn = 0;
	private int[] guardaComecoLaco = new int[1000];
	private int guaCoLa = 0;
	private int[] guardaFimLaco = new int[1000];
	private int guaFiLa = 0;
	private int pulaFimLaco;
	// private int pulaFimLaco = 0;

	public Gerador(No raiz, List<Simbolo> simbolos) {
		this.setRaiz(raiz);
		this.setSimbolos(simbolos);
	}

	public void gerar() throws IOException {
		arquivo = new File("saida.lvm");
		printWriter = new FileOutputStream(arquivo);

		arqAsm = new File("saida.lasm");
		printAsm = new PrintWriter(arqAsm);

		contaInstrucoes();

		magicWorld();

		gerar(raiz);

		imprimeTamInstrucoes();
		imprimeTamDados();
		imprimeInstrucoes();
		imprimeDados();

		printWriter.write(0xff);

		printAsm.flush();
		printAsm.close();

		printWriter.flush();
		printWriter.close();

		// System.out.println("\nTabela de Simbolos depois do gerador:");
		// for (int i = 0; i < simbolos.size(); i++) {
		// System.out.print(" " + i);
		// System.out.print(" " + simbolos.get(i).getImagem());
		// System.out.print(" " + simbolos.get(i).getEscopo());
		// System.out.print(" " + simbolos.get(i).getTipo());
		// System.out.print(" " + simbolos.get(i).getIndiceVar());
		// System.out.print(" " + simbolos.get(i).getValor());
		// System.out.print(" " + simbolos.get(i).getString());
		// System.out.println(" " + simbolos.get(i).getTamVar());
		// }

		// System.out.println("\nTabela de dados:");
		// for (int i = 0; i < dados.size(); i++) {
		// System.out.print(" " + i);
		// System.out.print(" " + dados.get(i).getDado());
		// System.out.print(" " + dados.get(i).getIndiceDado());
		// System.out.print(" " + dados.get(i).getTamDado());
		// System.out.println(" " + dados.get(i).getTipo());
		// }
	}

	private Object gerar(No no) {
		if (no.getTipo().equals("NO_TOKEN")) {
			return null;
		} else if (no.getTipo().equals("NO_LIST_CMD")) {
			return listCmd(no);
		} else if (no.getTipo().equals("NO_CMD")) {
			return cmd(no);
		} else if (no.getTipo().equals("NO_CMD_INTER")) {
			return cmdInter(no);
		} else if (no.getTipo().equals("NO_ESCRITA")) {
			return escrita(no);
		} else if (no.getTipo().equals("NO_EXP_ARIT")) {
			return expArit(no);
		} else if (no.getTipo().equals("NO_OPERAN")) {
			return operan(no);
		} else if (no.getTipo().equals("NO_OP_ARIT")) {
			return opArit(no);
		} else if (no.getTipo().equals("NO_DECL")) {
			return decl(no);
		} else if (no.getTipo().equals("NO_TIPO")) {
			return tipo(no);
		} else if (no.getTipo().equals("NO_LIST_ID")) {
			return listId(no);
		} else if (no.getTipo().equals("NO_LIST_ID_2")) {
			return listId2(no);
		} else if (no.getTipo().equals("NO_LEITURA")) {
			return leitura(no);
		} else if (no.getTipo().equals("NO_ATRIB")) {
			return atrib(no);
		} else if (no.getTipo().equals("NO_COND")) {
			return cond(no);
		} else if (no.getTipo().equals("NO_EXP_LOG")) {
			return expLog(no);
		} else if (no.getTipo().equals("NO_EXP_REL")) {
			return expRel(no);
		} else if (no.getTipo().equals("NO_OP_REL")) {
			return opRel(no);
		} else if (no.getTipo().equals("NO_OP_LOG")) {
			return opLog(no);
		} else if (no.getTipo().equals("NO_SENAO")) {
			return senao(no);
		} else if (no.getTipo().equals("NO_LACO")) {
			return laco(no);
		} else {
			addErro("Erro: NO desconhecido = " + no.getTipo());
			return null;
		}
	}

	// <laco> ::= 'enquanto' <exp_log> <list_cmd>
	private Object laco(No no) {

		lacoInstAntes[laInAn] = contaInstrucoes();
		// System.out.println("lacoInstAntes[laInAn]" + lacoInstAntes[laInAn]);
		laInAn++;

		gerar(no.getFilhos().get(1));

		// System.out.println("Laco Comeca = " + contaInstrucoes());
		guardaComecoLaco[guaCoLa] = contaInstrucoes();
		guaCoLa++;

		gerar(no.getFilhos().get(2));

		laInAn--;
		int numero = contaInstrucoes() - lacoInstAntes[laInAn] + 6;
		printAsm.println("jump laco, " + retornaStringHex(numero) + "h");
		insereInstrucao(16, 1, 8, 1, numero, 4);

		// System.out.println("Laco Termina = " + contaInstrucoes());
		guardaFimLaco[guaFiLa] = contaInstrucoes();

		guaCoLa--;
		pulaFimLaco = contaInstrucoes() - guardaComecoLaco[guaCoLa];

		// System.out.println("Laco pulaFimLaco = " + pulaFimLaco);

		indVar--;
		dados.get(indexIndiceVar[indVar]).setDado(String.valueOf(pulaFimLaco));
		dados.get(indexIndiceVar[indVar]).setTamDado(4);
		dados.get(indexIndiceVar[indVar]).setTipo("object");

		guaFiLa++;

		printAsm.println();
		return null;
	}

	// <senao> ::= '{' <list_cmd> '}' |
	private Object senao(No no) {
		if (!no.getFilhos().isEmpty()) {
			gerar(no.getFilhos().get(1));
		}
		// printAsm.println();
		return null;
	}

	// <op_log> ::= '&&' | '||'
	private Object opLog(No no) {
		return no.getFilhos().get(0).getToken().getImagem();
	}

	// <op_rel> ::= '>' | '<' | '>=' | '<=' | '==' | '!='
	private Object opRel(No no) {
		return no.getFilhos().get(0).getToken().getImagem();
	}

	// <exp_rel> ::= <op_rel> <operan> <operan> | <op_log> '{' <exp_rel> '}' '{'
	// <exp_rel> '}'
	private Object expRel(No no) {
		if (no.getFilhos().get(0).getTipo().equals("NO_OP_REL")) {

			Object sinal = gerar(no.getFilhos().get(0));

			// System.out.println("sinal = " + sinal);

			Token operan1 = (Token) gerar(no.getFilhos().get(1));
			// System.out.println("operan1 = " + operan1);

			Token operan2 = (Token) gerar(no.getFilhos().get(2));
			// System.out.println("operan2 = " + operan2);

			if (operan2.getClasse().equals("CLI")) {
				int numero = Integer.parseInt(operan2.getImagem().toString());
				printAsm.println("mov intControl, " + retornaStringHex(numero) + "h");
				insereInstrucao(6, 1, 2, 1, numero, 4);
			} else if (operan2.getClasse().equals("ID")) {
				int numero = simbolos.get(operan2.getIndice()).getIndiceVar();
				printAsm.println("mov intControl, [" + retornaStringHex(numero) + "h]");
				insereInstrucao(5, 1, 2, 1, numero, 4);
			}

			if (operan1.getClasse().equals("CLI")) {
				int numero = Integer.parseInt(operan1.getImagem().toString());
				printAsm.println("cmp intControl, " + retornaStringHex(numero) + "h");
				insereInstrucao(14, 1, 2, 1, numero, 4);
			} else if (operan1.getClasse().equals("ID")) {
				int numero = simbolos.get(operan1.getIndice()).getIndiceVar();
				printAsm.println("cmp intControl, [" + retornaStringHex(numero) + "h]");
				insereInstrucao(13, 1, 2, 1, numero, 4);
			}

			Dado dd1 = new Dado();

			dd1.setIndiceDado(indiceVar);

			if (sinal.equals(">")) {
				printAsm.println("jump jle, " + retornaStringHex(indiceVar) + "h");
				insereInstrucao(16, 1, 4, 1, indiceVar, 4);
				indiceVar += 4;
			} else if (sinal.equals("<")) {
				printAsm.println("jump jge, " + retornaStringHex(indiceVar) + "h");
				insereInstrucao(16, 1, 2, 1, indiceVar, 4);
				indiceVar += 4;
			} else if (sinal.equals(">=")) {
				printAsm.println("jump jl, " + retornaStringHex(indiceVar) + "h");
				insereInstrucao(16, 1, 3, 1, indiceVar, 4);
				indiceVar += 4;
			} else if (sinal.equals("<=")) {
				printAsm.println("jump jg, " + retornaStringHex(indiceVar) + "h");
				insereInstrucao(16, 1, 1, 1, indiceVar, 4);
				indiceVar += 4;
			} else if (sinal.equals("==")) {
				printAsm.println("jump jne, " + retornaStringHex(indiceVar) + "h");
				insereInstrucao(16, 1, 6, 1, indiceVar, 4);
				indiceVar += 4;
			} else if (sinal.equals("!=")) {
				printAsm.println("jump je, " + retornaStringHex(indiceVar) + "h");
				insereInstrucao(16, 1, 5, 1, indiceVar, 4);
				indiceVar += 4;
			}

			dados.add(dd1);
			indexIndiceVar[indVar] = dados.indexOf(dd1);
			indVar++;

		} else if (no.getFilhos().get(0).getTipo().equals("NO_OP_LOG")) {
			gerar(no.getFilhos().get(2));
			gerar(no.getFilhos().get(5));
		}

		printAsm.println();
		return null;
	}

	// <exp_log> ::= '{' <exp_rel> '}'
	private Object expLog(No no) {
		return gerar(no.getFilhos().get(1));
	}

	// <cond> ::= 'se' <exp_log> '{' <list_cmd> '}' <senao>
	private Object cond(No no) {
		gerar(no.getFilhos().get(1));

		verdadeAntes[verdAnt] = contaInstrucoes();
		verdAnt++;

		gerar(no.getFilhos().get(3));

		verdAnt--;
		pulaSenao = contaInstrucoes() - verdadeAntes[verdAnt];

		indVar--;
		dados.get(indexIndiceVar[indVar]).setDado(String.valueOf(pulaSenao + 6));
		dados.get(indexIndiceVar[indVar]).setTamDado(4);
		dados.get(indexIndiceVar[indVar]).setTipo("object");

		printAsm.println("jump fimse, " + retornaStringHex(indiceVar) + "h");
		insereInstrucao(16, 1, 7, 1, indiceVar, 4);

		Dado dd2 = new Dado();
		dd2.setIndiceDado(indiceVar);
		dd2.setTipo("object");
		indiceVar += 4;
		dados.add(dd2);
		indexdd2[indDd2] = dados.indexOf(dd2);
		indDd2++;

		falsoAntes[falAnt] = contaInstrucoes();
		falAnt++;

		gerar(no.getFilhos().get(5));

		falAnt--;
		pulaFimse = contaInstrucoes() - falsoAntes[falAnt];

		indDd2--;
		dados.get(indexdd2[indDd2]).setDado(String.valueOf(pulaFimse));
		dados.get(indexdd2[indDd2]).setTamDado(4);

		printAsm.println();
		return null;
	}

	// <atrib> ::= '=' id <exp_arit>
	private Object atrib(No no) {
		Token id = no.getFilhos().get(1).getToken();

		String tipo = simbolos.get(id.getIndice()).getTipo();

		// System.out.println("Atrib id = " + id + " tipo = " + tipo);

		Token valorExpArit = (Token) gerar(no.getFilhos().get(2));

		// System.out.println("Atrib valorExpArit = " + valorExpArit);

		if (valorExpArit != null) {
			if (valorExpArit.getClasse().equals("CLI")) {
				int numero = Integer.parseInt(valorExpArit.getImagem().toString());
				printAsm.println("mov intControl, " + retornaStringHex(numero) + "h");
				insereInstrucao(6, 1, 2, 1, numero, 4);
			} else if (valorExpArit.getClasse().equals("ID")) {
				// int numero =
				// simbolos.get(valorExpArit.getIndice()).getIndiceVar();
				// printAsm.println("mov eax, [" + retornaStringHex(numero) +
				// "h]");
				// insereInstrucao(3, 1, 1, 1, numero, 4);
			} else if (valorExpArit.getClasse().equals("CLS")) {
				// System.out.println("valorExpArit = " + valorExpArit);
				// int numero = simbolos.get(id.getIndice()).getIndiceVar();
				// System.out.println("numero = " + numero);
				// printAsm.println("mov string, [" + retornaStringHex(numero) +
				// "h]");
				// insereInstrucao(11, 1, 3, 1, numero, 4);
				//
				// String string = valorExpArit.getImagem();
				//
				//
				//
				// int tamStr = string.length() + 1 - contaBarra(string);
				//
				// simbolos.get(id.getIndice()).setValor(indiceVar);
				// System.out.println("indiceVar = " + indiceVar);
				// indiceVar += tamStr;
				// simbolos.get(id.getIndice()).setTamVar(tamStr);
				// simbolos.get(id.getIndice()).setString(string);

			}
		}

		if (tipo.equals("int")) {
			int numero = simbolos.get(id.getIndice()).getIndiceVar();
			printAsm.println("mov [" + retornaStringHex(numero) + "h], intControl");
			insereInstrucao(4, 1, 2, 1, numero, 4);
		} else if (tipo.equals("texto")) {
			// int numero = simbolos.get(id.getIndice()).getIndiceVar();
			// printAsm.println("mov [" + retornaStringHex(numero) + "h],
			// string");
			// insereInstrucao(12, 1, 3, 1, numero, 4);
		}

		printAsm.println();
		return null;
	}

	// <leitura> ::= 'le' id
	private Object leitura(No no) {
		Token id = no.getFilhos().get(1).getToken();
		String tipo = simbolos.get(id.getIndice()).getTipo();
		if (tipo.equals("int")) {
			int numero = dados.get(id.getIndice()).getIndiceDado();
			printAsm.println("int [" + retornaStringHex(numero) + "h], bufferInt");
			insereInstrucao(3, 1, 4, 1, numero, 4);
		} else if (tipo.equals("real")) {

		} else if (tipo.equals("texto")) {

		}

		printAsm.println();
		return null;
	}

	// <list_id2> ::= <list_id> |
	private Object listId2(No no) {
		if (no.getFilhos().isEmpty()) {
			return new ArrayList<Token>();
		} else {
			return gerar(no.getFilhos().get(0));
		}
	}

	// <list_id> ::= id <list_id2>
	@SuppressWarnings("unchecked")
	private Object listId(No no) {
		Token id = no.getFilhos().get(0).getToken();
		List<Token> listId2 = (List<Token>) gerar(no.getFilhos().get(1));
		listId2.add(0, id);

		return listId2;
	}

	// <tipo> ::= 'int' | 'real' | 'texto' | 'logico'
	private Object tipo(No no) {
		return no.getFilhos().get(0).getToken().getImagem();
	}

	// <decl> ::= <tipo> <list_id>
	@SuppressWarnings("unchecked")
	private Object decl(No no) {
		List<Token> listId = (List<Token>) gerar(no.getFilhos().get(1));

		for (Token id : listId) {

			String tipo = simbolos.get(id.getIndice()).getTipo();

			if (tipo.equals("int")) {

				simbolos.get(id.getIndice()).setIndiceVar(indiceVar);

				Dado dd = new Dado();
				dd.setDado(String.valueOf(0));
				dd.setIndiceDado(indiceVar);
				dd.setTamDado(4);
				dd.setTipo("int");
				dados.add(dd);
				printAsm.println("[" + retornaStringHex(indiceVar) + "h] ");
				indiceVar += 4;

			} else if (tipo.equals("texto")) {

			}
		}

		printAsm.println();
		return null;
	}

	// <op_arit> ::= '+' | '-' | '*' | '/' | '.'
	private Object opArit(No no) {
		return no.getFilhos().get(0).getToken().getImagem();
	}

	// <operan> ::= id | cli | clr | cls
	private Object operan(No no) {
		return no.getFilhos().get(0).getToken();
	}

	// <exp_arit> ::= <operan> | '{' <op_arit> <exp_arit> <exp_arit> '}'
	private Object expArit(No no) {

		if (no.getFilhos().size() == 1) {
			return gerar(no.getFilhos().get(0));
		} else if (no.getFilhos().size() == 5) {

			Object operador = gerar(no.getFilhos().get(1));

			Token valor2 = (Token) gerar(no.getFilhos().get(3));

			// System.out.println("valor2 = " + valor2);

			if (valor2 != null) {
				if (valor2.getClasse().equals("CLI")) {
					if (operador.equals(".")) {

						String string = valor2.getImagem();
						int tamDado = 4;
						Dado dd = new Dado();
						dd.setDado(string);
						dd.setIndiceDado(indiceVar);
						dd.setTamDado(tamDado);
						dd.setTipo("texto");
						dados.add(dd);

						printAsm.println("convert strControl, " + retornaStringHex(Integer.parseInt(string)) + "h");
						insereInstrucao(17, 1, 1, 1, Integer.parseInt(string), 4);
						indiceVar += tamDado;
					} else {
						int numero = Integer.parseInt(valor2.getImagem().toString());
						printAsm.println("mov intControl, " + retornaStringHex(numero) + "h");
						insereInstrucao(6, 1, 2, 1, numero, 4);
					}

				} else if (valor2.getClasse().equals("ID")) {
					if (operador.equals(".")) {
						int numero = dados.get(valor2.getIndice()).getIndiceDado();
						printAsm.println("convert strControl, [" + retornaStringHex(numero) + "h]");
						insereInstrucao(11, 1, 1, 1, numero, 4);
					} else {
						// System.out.println("Este valor2 = " + valor2);
						int numero = dados.get(valor2.getIndice()).getIndiceDado();

						printAsm.println("mov intControl, [" + retornaStringHex(numero) + "h]");
						insereInstrucao(5, 1, 2, 1, numero, 4);
					}

				} else if (valor2.getClasse().equals("CLS")) {
					// System.out.println("valor2 = " + valor2);
					String string = valor2.getImagem();
					int tamDado = string.length() + 1 - contaBarras(string);

					Dado dd = new Dado();
					dd.setDado(string);
					dd.setIndiceDado(indiceVar);
					dd.setTamDado(tamDado);
					dd.setTipo("texto");
					dados.add(dd);

					printAsm.println("mov strControl, [" + retornaStringHex(indiceVar) + "h]");
					insereInstrucao(1, 1, 1, 1, indiceVar, 4);
					indiceVar += tamDado;
				}
			}

			Token valor1 = (Token) gerar(no.getFilhos().get(2));
			// System.out.println("valor1 = " + valor1);

			if (valor1.getClasse().equals("ID")) {
				if (operador.equals(".")) {
					String tipo = simbolos.get(valor1.getIndice()).getTipo();
					if (tipo.equals("int")) {
						int numero = simbolos.get(valor1.getIndice()).getIndiceVar();

						printAsm.println("concat strControlConvert, [" + retornaStringHex(numero) + "h]");
						insereInstrucao(18, 1, 1, 1, numero, 4);
					}

				} else if (operador.equals("+")) {
					String tipo = simbolos.get(valor1.getIndice()).getTipo();
					if (tipo.equals("int")) {
						int numero = simbolos.get(valor1.getIndice()).getIndiceVar();

						printAsm.println("add intControl, [" + retornaStringHex(numero) + "h]");
						insereInstrucao(7, 1, 2, 1, numero, 4);
					}
				} else if (operador.equals("-")) {
					String tipo = simbolos.get(valor1.getIndice()).getTipo();
					if (tipo.equals("int")) {
						int numero = simbolos.get(valor1.getIndice()).getIndiceVar();

						printAsm.println("sub intControl, [" + retornaStringHex(numero) + "h]");
						insereInstrucao(8, 1, 2, 1, numero, 4);
					}
				} else if (operador.equals("*")) {
					String tipo = simbolos.get(valor1.getIndice()).getTipo();
					if (tipo.equals("int")) {
						int numero = simbolos.get(valor1.getIndice()).getIndiceVar();

						printAsm.println("mult intControl, [" + retornaStringHex(numero) + "h]");
						insereInstrucao(9, 1, 2, 1, numero, 4);
					}
				} else if (operador.equals("/")) {
					String tipo = simbolos.get(valor1.getIndice()).getTipo();
					if (tipo.equals("int")) {
						int numero = simbolos.get(valor1.getIndice()).getIndiceVar();

						printAsm.println("mult intControl, [" + retornaStringHex(numero) + "h]");
						insereInstrucao(10, 1, 2, 1, numero, 4);
					}
				}

			}
			if (valor1.getClasse().equals("CLS")) {
				if (operador.equals(".")) {
					// System.out.println("valor1 = " + valor1);
					String string = valor1.getImagem();
					int tamDado = string.length() + 1 - contaBarras(string);

					Dado dd = new Dado();
					dd.setDado(string);
					dd.setIndiceDado(indiceVar);
					dd.setTamDado(tamDado);
					dd.setTipo("texto");
					dados.add(dd);

					printAsm.println("concat strControl, [" + retornaStringHex(indiceVar) + "h]");
					insereInstrucao(12, 1, 1, 1, indiceVar, 4);
					indiceVar += tamDado;
				}

			}

			return null;
		}

		printAsm.println();
		return null;
	}

	// <escrita> ::= 'mostra' <exp_arit>
	private Object escrita(No no) {

		Token token = (Token) gerar(no.getFilhos().get(1));

		if (token != null) {

			if (token.getClasse().equals("CLS")) {
				// System.out.println("token = " + token);
				String string = token.getImagem();
				int tamDado = string.length() + 1 - contaBarras(string);

				Dado dd = new Dado();
				dd.setDado(string);
				dd.setIndiceDado(indiceVar);
				dd.setTamDado(tamDado);
				dd.setTipo("texto");
				dados.add(dd);

				printAsm.println("mov strControl, [" + retornaStringHex(indiceVar) + "h]");
				insereInstrucao(1, 1, 1, 1, indiceVar, 4);
				indiceVar += tamDado;
			} else if (token.getClasse().equals("ID")) {
				// System.out.println("Token = " + token);

				int numero = simbolos.get(token.getIndice()).getIndiceVar();
				printAsm.println("convert strControl, [" + retornaStringHex(numero) + "h]");
				insereInstrucao(11, 1, 1, 1, numero, 4);
			}
		}

		printAsm.println("int 00000000h, strControl");
		insereInstrucao(2, 1, 1, 1, 0, 4);

		printAsm.println();
		return null;
	}

	// <cmd_inter> ::= <decl> | <atrib> | <laco> | <cond> | <escrita> |
	// <leitura>
	private Object cmdInter(No no) {
		gerar(no.getFilhos().get(0));
		return null;
	}

	// <cmd> ::= '{' <cmd_inter> '}'
	private Object cmd(No no) {
		gerar(no.getFilhos().get(1));
		return null;
	}

	// <list_cmd> ::= <cmd> <list_cmd> | &
	private Object listCmd(No no) {
		if (!no.getFilhos().isEmpty()) {
			gerar(no.getFilhos().get(0));
			gerar(no.getFilhos().get(1));
		}
		return null;
	}

	// FIM DAS REGRAS

	public void addErro(String erro) {
		erros.add("Erro no gerador binário: " + erro + ". ");
	}

	public boolean temErros() {
		return !erros.isEmpty();
	}

	public void mostraErros() {
		for (String erro : erros) {
			System.out.println(erro);
		}
	}

	public No getRaiz() {
		return raiz;
	}

	public void setRaiz(No raiz) {
		this.raiz = raiz;
	}

	public List<Simbolo> getSimbolos() {
		return simbolos;
	}

	public void setSimbolos(List<Simbolo> simbolos) {
		this.simbolos = simbolos;
	}

	public List<String> getErros() {
		return erros;
	}

	public void setErros(List<String> erros) {
		this.erros = erros;
	}

	// INICIO METODOS

	private int contaInstrucoes() {
		int contaBytes = 0;
		for (int i = 0; i < instrucoes.size(); i++) {
			contaBytes += instrucoes.get(i).getTamByte1() + instrucoes.get(i).getTamByte2()
					+ instrucoes.get(i).getTamByte3();
		}
		return contaBytes;
	}

	private void magicWorld() throws IOException {
		printWriter.write(0xba);
		printWriter.write(0xba);
		printWriter.write(0xba);
		printWriter.write(0xbe);
	}

	private void imprimeTamInstrucoes() throws IOException {

		int contaBytes = 0;
		for (int i = 0; i < instrucoes.size(); i++) {
			contaBytes += instrucoes.get(i).getTamByte1() + instrucoes.get(i).getTamByte2()
					+ instrucoes.get(i).getTamByte3();
		}
		escreveHex(contaBytes);

	}

	private void escreveHex(int numero) throws IOException {
		int result1 = ((numero >> 24) & 0xFF);
		int result2 = ((numero >> 16) & 0xFF);
		int result3 = ((numero >> 8) & 0xFF);
		int result4 = ((numero) & 0xFF);
		printWriter.write(result1);
		printWriter.write(result2);
		printWriter.write(result3);
		printWriter.write(result4);

	}

	private void imprimeTamDados() throws IOException {
		int contaBytes = 0;
		for (int i = 0; i < dados.size(); i++) {
			contaBytes += dados.get(i).getTamDado();
			// System.out.println("dados = " + simbolos.get(i).getImagem());
		}
		escreveHex(contaBytes);
	}

	private String retornaStringHex(int numero) {
		int result1 = ((numero >> 24) & 0xFF);
		int result2 = ((numero >> 16) & 0xFF);
		int result3 = ((numero >> 8) & 0xFF);
		int result4 = ((numero) & 0xFF);

		String string = "" + String.format("%02X", result1) + String.format("%02X", result2)
				+ String.format("%02X", result3) + String.format("%02X", result4);

		return string;
	}

	private int contaBarras(String string) {
		int contBarra = 0;
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) == '\\') {
				i++;
				contBarra++;
			}
		}
		return contBarra;
	}

	private void insereInstrucao(int valor1, int tamByte1, int valor2, int tamByte2, int valor3, int tamByte3) {
		Instrucao instrucao = new Instrucao();
		instrucao.setValor1(valor1);
		instrucao.setTamByte1(tamByte1);
		instrucao.setValor2(valor2);
		instrucao.setTamByte2(tamByte2);
		instrucao.setValor3(valor3);
		instrucao.setTamByte3(tamByte3);
		instrucoes.add(instrucao);
	}

	private void imprimeInstrucoes() throws IOException {
		for (int i = 0; i < instrucoes.size(); i++) {
			escreveInstrucao(instrucoes.get(i).getValor1(), instrucoes.get(i).getTamByte1());
			escreveInstrucao(instrucoes.get(i).getValor2(), instrucoes.get(i).getTamByte2());
			escreveInstrucao(instrucoes.get(i).getValor3(), instrucoes.get(i).getTamByte3());
		}
	}

	private void escreveInstrucao(int numero, int tamByte) throws IOException {
		if (tamByte == 1) {
			printWriter.write(numero);
		} else if (tamByte == 4) {
			escreveHex(numero);
		}
	}

	private void imprimeDados() throws IOException {
		for (int i = 0; i < dados.size(); i++) {
			String tipo = dados.get(i).getTipo();

			// System.out.println("tipo = " + tipo);

			if (tipo != null) {
				if (tipo.equals("int")) {
					int valor = Integer.parseInt(dados.get(i).getDado());
					escreveHex(valor);
				} else if (tipo.equals("object")) {
					int valor = Integer.parseInt(dados.get(i).getDado());
					// System.out.println("valor se = " + valor + " i = " + i);
					// System.out.println("Indice = " +
					// dados.get(i).getIndiceDado());
					escreveHex(valor);
				} else if (tipo.equals("texto")) {

					String string = dados.get(i).getDado();

					for (int j = 0; j < string.length(); j++) {
						if (string.charAt(j) == '\\') {
							j++;
							if (string.charAt(j) == '\\') {
								printWriter.write(0x5c);
							} else if (string.charAt(j) == 'n') {
								printWriter.write(0x0a);
							} else if (string.charAt(j) == 't') {
								printWriter.write(0x09);
							}
						} else {
							printWriter.write(string.charAt(j));
						}
					}
					printWriter.write(0x0);
				}
			}

		}
	}

}
