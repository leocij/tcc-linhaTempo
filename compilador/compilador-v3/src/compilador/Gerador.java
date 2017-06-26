package compilador;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Gerador {

	private No raiz;
	private List<Simbolo> simbolos;
	private List<String> erros = new ArrayList<String>();
	private PrintWriter printWriter;
	private File arquivo;
	private List<Instrucao> instrucoes = new ArrayList<>();
	private List<GuardaStr> guardaStrs = new ArrayList<>();
	//private int contStr = 0;
	private int juntaTam = 0;

	public Gerador(No raiz, List<Simbolo> simbolos) {
		this.setRaiz(raiz);
		this.setSimbolos(simbolos);
	}

	public void gerar() throws FileNotFoundException {
		arquivo = new File("saida.lvm");
		printWriter = new PrintWriter(arquivo);
		printWriter.write(0xba);
		printWriter.write(0xba);
		printWriter.write(0xba);
		printWriter.write(0xbe);
		gerar(raiz);
		tamCodigo(instrucoes);
		tamDados(simbolos);
		imprimeCodigo(instrucoes);
		imprimeDados(simbolos);
		imprimeString(guardaStrs);
		printWriter.write(0xff);
		printWriter.flush();
		printWriter.close();
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
		} else if (no.getTipo().equals("NO_DECL")) {
			return decl(no);
		} else if (no.getTipo().equals("NO_TIPO")) {
			return tipo(no);
		} else if (no.getTipo().equals("NO_LIST_ID")) {
			return listId(no);
		} else if (no.getTipo().equals("NO_LIST_ID_2")) {
			return listId2(no);
		} else if (no.getTipo().equals("NO_ATRIB")) {
			return atrib(no);
		} else if (no.getTipo().equals("NO_EXP_ARIT")) {
			return expArit(no);
		} else if (no.getTipo().equals("NO_OPERAN")) {
			return operan(no);
		} else if (no.getTipo().equals("NO_OP_ARIT")) {
			return opArit(no);
		} else if (no.getTipo().equals("NO_ESCRITA")) {
			return escrita(no);
		} else {
			addErro("No desconhecido = " + no.getTipo());
			return null;
		}

	}

	// <escrita> ::= 'mostra' <exp_arit>
	private Object escrita(No no) {
		Token string = (Token) gerar(no.getFilhos().get(1));

		if (string.getClasse().equals("CLS")) {
			insereString(5, 1, 5, 1, string.getImagem());
		} else if (string.getClasse().equals("ID")) {
			System.out.println("Valor ID = " + string.getImagem() + ", valor = " + simbolos.get(string.getIndice()).getValor());
			insereString(5, 1, 5, 1, simbolos.get(string.getIndice()).getValor().toString());
		}

		return null;
	}

	// <op_arit> ::= '+' | '-' | '*' | '/' | '.'
	private Object opArit(No no) {
		//System.out.println("opArit = " + no.getFilhos().get(0).getToken().getImagem());
		return no.getFilhos().get(0).getToken().getImagem();
	}

	// <operan> ::= id | cli | clr | cls
	private Object operan(No no) {
		return (Token) no.getFilhos().get(0).getToken();
	}

	// <exp_arit> ::= <operan> | '{' <op_arit> <exp_arit> <exp_arit> '}'
	private Object expArit(No no) {

		if (no.getFilhos().size() == 1) {
			return gerar(no.getFilhos().get(0));
		} else if (no.getFilhos().size() == 5) {

			Token valor2 = (Token) gerar(no.getFilhos().get(3));

			if (valor2 != null) {
				if (valor2.getClasse().equals("ID")) {
					// System.out.println("0101" + String.format("%08X",
					// simbolos.get(valor2.getIndice()).getMemoria()));
					System.out.println("MOV EAX, " + valor2.getImagem());
					insereInstrucao(1, 1, 1, 1, simbolos.get(valor2.getIndice()).getMemoria(), 4);
				} else if (valor2.getClasse().equals("CLI")) {
					// System.out.println("0401" + String.format("%08x",
					// Integer.parseInt(valor2.getImagem())));
					System.out.println("MOV EAX, " + valor2.getImagem());
					insereInstrucao(4, 1, 1, 1, Integer.parseInt(valor2.getImagem()), 4);
				}
			}

			Object operador = gerar(no.getFilhos().get(1));

			Token valor1 = (Token) gerar(no.getFilhos().get(2));
			if (operador.equals("+")) {
				if (valor1.getClasse().equals("ID")) {
					// System.out.println("0201" + String.format("%08X",
					// simbolos.get(valor1.getIndice()).getMemoria()));
					System.out.println("ADD EAX, " + valor1.getImagem());
					insereInstrucao(2, 1, 1, 1, simbolos.get(valor1.getIndice()).getMemoria(), 4);

				} else if (valor1.getClasse().equals("CLI")) {
					// System.out.println("0701" + String.format("%08x",
					// Integer.parseInt(valor1.getImagem())));
					System.out.println("ADD EAX, " + valor1.getImagem());
					insereInstrucao(7, 1, 1, 1, Integer.parseInt(valor1.getImagem()), 4);
				}

			}

		}

		return null;
	}

	// <atrib> ::= '=' id <exp_arit>
	private Object atrib(No no) {
		Token id = no.getFilhos().get(1).getToken();
		Token operando = (Token) gerar(no.getFilhos().get(2));

		if (operando != null) {
			if (operando.getClasse().equals("CLI")) {
				simbolos.get(id.getIndice()).setValor(operando.getImagem());
			} else if (operando.getClasse().equals("ID")) {
				// System.out.println("08" + String.format("%08x",
				// simbolos.get(id.getIndice()).getMemoria())
				// + String.format("%08x",
				// simbolos.get(operando.getIndice()).getMemoria()));

				System.out.println("MOV EAX, " + operando.getImagem());
				insereInstrucao(1, 1, 1, 1, simbolos.get(operando.getIndice()).getMemoria(), 4);

				System.out.println("MOV " + id.getImagem() + ", EAX");
				insereInstrucao(3, 1, simbolos.get(id.getIndice()).getMemoria(), 4, 1, 1);
			}
		} else {
			// System.out.println("03" + String.format("%08x",
			// simbolos.get(id.getIndice()).getMemoria()) + "01");
			System.out.println("MOV " + id.getImagem() + ", EAX");
			insereInstrucao(3, 1, simbolos.get(id.getIndice()).getMemoria(), 4, 1, 1);
		}

		return null;
	}

	// <list_id2> ::= <list_id> | &
	private Object listId2(No no) {
		if (!no.getFilhos().isEmpty()) {
			gerar(no.getFilhos().get(0));
		}
		return null;
	}

	// <list_id> ::= id <list_id2>
	private Object listId(No no) {
		Token id = (Token) no.getFilhos().get(0).getToken();
		String tipo = (String) simbolos.get(id.getIndice()).getTipo();
		if (tipo.equals("int")) {
			simbolos.get(id.getIndice()).setValor("0");
		} else if (tipo.equals("real")) {
			// printWriter.write(" " + id.getImagem() + " = 0.0");
		} else if (tipo.equals("texto")) {
			// printWriter.write(" " + id.getImagem() + "[1000]");
		}
		gerar(no.getFilhos().get(1));
		return null;
	}

	// <tipo> ::= 'int' | 'real' | 'texto' | 'logico'
	private Object tipo(No no) {
		gerar(no.getFilhos().get(0));
		return null;
	}

	// <decl> ::= <tipo> <list_id>
	private Object decl(No no) {
		gerar(no.getFilhos().get(0));
		gerar(no.getFilhos().get(1));
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
		erros.add("Erro de geração: " + erro + ". ");
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

	private void escreveHex(int numero) {
		int result1 = ((numero >> 24) & 0xFF);
		int result2 = ((numero >> 16) & 0xFF);
		int result3 = ((numero >> 8) & 0xFF);
		int result4 = ((numero) & 0xFF);
		printWriter.write(result1);
		printWriter.write(result2);
		printWriter.write(result3);
		printWriter.write(result4);
	}

	private void tamCodigo(List<Instrucao> instrucoes2) {
		int countBytes = 0;
		for (int i = 0; i < instrucoes2.size(); i++) {
			countBytes += instrucoes2.get(i).getTamByte1() + instrucoes2.get(i).getTamByte2()
					+ instrucoes2.get(i).getTamByte3();
		}
		//System.out.println("QuantBytes = " + countBytes);

		escreveHex(countBytes);
	}

	private void tamDados(List<Simbolo> simbolos2) {
		int tamDado = 0;

		for (int i = 0; i < simbolos2.size(); i++) {
			tamDado = tamDado + 4;
		}

		escreveHex(tamDado);
	}

	private void imprimeCodigo(List<Instrucao> instrucoes2) {
		for (int i = 0; i < instrucoes2.size(); i++) {
			escreveInstrucao(instrucoes2.get(i).getValor1(), instrucoes2.get(i).getTamByte1());
			escreveInstrucao(instrucoes2.get(i).getValor2(), instrucoes2.get(i).getTamByte2());
			escreveInstrucao(instrucoes2.get(i).getValor3(), instrucoes2.get(i).getTamByte3());
		}
	}

	private void escreveInstrucao(int valor1, int tamByte1) {
		if (tamByte1 == 1) {
			printWriter.write(valor1);
		} else if (tamByte1 == 4) {
			escreveHex(valor1);
		}
	}

	private void imprimeDados(List<Simbolo> simbolos2) {
		int numero = 0;
		for (int i = 0; i < simbolos2.size(); i++) {
			numero = Integer.parseInt(simbolos2.get(i).getValor().toString());

			escreveHex(numero);
		}
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

	private void insereString(int identificador, int tamIdent, int saida, int tamSaida, String string) {
		// System.out.println("identificador = " + identificador + ", tam = " +
		// tamIdent);
		// System.out.println("saida = " + saida + ", tam = " + tamSaida);
		// System.out.println("String = " + string);

		int contBarra = 0;

		for (int i = 0; i < string.length() - 1; i++) {
			if (string.charAt(i) == 0x5c) {
				contBarra++;
				i++;
			}
		}

		//System.out.println("Barra = " + contBarra);

		int tamStr = string.length();

		//System.out.println("tamStr = " + tamStr);
		
		insereInstrucao(identificador, tamIdent, saida, tamSaida, juntaTam, 4);

//		if (contStr == 0) {
//			insereInstrucao(identificador, tamIdent, saida, tamSaida, juntaTam, 4);
//		} else {
//			insereInstrucao(identificador, tamIdent, saida, tamSaida, juntaTam, 4);
//		}

		System.out.println("MOV SAIDA, [" + String.format("%02X", (juntaTam>>24)&0xFF) + String.format("%02X", (juntaTam>>16)&0xFF) + String.format("%02X", (juntaTam>>8)&0xFF) + String.format("%02X", (juntaTam>>0)&0xFF) + "]");

		juntaTam += 4 + tamStr - contBarra;

		//System.out.println("juntaTam = " + juntaTam);

		GuardaStr guardaStr = new GuardaStr();
		// guardaStr.setMemStr(contStr ++);
		guardaStr.setString(string);
		guardaStrs.add(guardaStr);

		//contStr++;

	}

	private void imprimeString(List<GuardaStr> guardaStrs2) {
		for (int i = 0; i < guardaStrs2.size(); i++) {
			// int memStr = guardaStrs2.get(i).getMemStr();
			// escreveHex(memStr);

			int contTam = 0;
			String string = guardaStrs2.get(i).getString();

			for (int j = 0; j < string.length(); j++) {
				if (string.charAt(j) == 0x5c) {
					j++;
					contTam++;

				} else {
					contTam++;
				}
			}

			// int tamStr = guardaStrs2.get(i).getString().length();

			escreveHex(contTam);

			for (int j = 0; j < string.length(); j++) {
				if (string.charAt(j) == 0x5c) {
					j++;
					if (string.charAt(j) == 0x5c) {
						printWriter.write(0x5c);
					} else if (string.charAt(j) == 0x6e) {
						printWriter.write(0x0a);
					} else if (string.charAt(j) == 0x74) {
						printWriter.write(0x09);
					}
				} else {
					printWriter.write(string.charAt(j));
				}
			}

		}
	}

}
